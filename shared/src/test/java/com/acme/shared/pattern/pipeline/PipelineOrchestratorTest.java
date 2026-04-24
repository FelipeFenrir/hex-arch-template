package com.acme.shared.pattern.pipeline;

import com.acme.shared.pattern.pipeline.PipelineContext;
import com.acme.shared.pattern.pipeline.PipelineOrchestrator;
import com.acme.shared.pattern.pipeline.RollbackStyle;
import com.acme.shared.pattern.pipeline.Step;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;
import com.acme.shared.stereotypes.test.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@UnitTest
@DisplayName("PipelineOrchestrator")
class PipelineOrchestratorTest {

    // ── test doubles ──────────────────────────────────────────────────────

    /** Minimal context that records the execution log. */
    static class TraceContext extends PipelineContext {
        final List<String> log = new ArrayList<>();
        final List<String> rollbackLog = new ArrayList<>();
    }

    /** Step that always succeeds and appends its id to the execution log. */
    static class OkStep implements Step<TraceContext> {
        private final String stepId;
        private final RollbackStyle style;

        OkStep(String stepId, RollbackStyle style) {
            this.stepId = stepId;
            this.style = style;
        }

        OkStep(String stepId) { this(stepId, RollbackStyle.NONE); }

        @Override public String id() { return stepId; }
        @Override public RollbackStyle rollbackStyle() { return style; }

        @Override
        public Result<Void, List<DomainError>> execute(TraceContext ctx) {
            ctx.log.add(stepId + ":execute");
            return Result.success(null);
        }

        @Override
        public void rollback(TraceContext ctx) {
            ctx.rollbackLog.add(stepId + ":rollback");
        }
    }

    /** Step that always fails with one domain error. */
    static class FailStep implements Step<TraceContext> {
        private final String stepId;
        private final String errorCode;

        FailStep(String stepId, String errorCode) {
            this.stepId = stepId;
            this.errorCode = errorCode;
        }

        @Override public String id() { return stepId; }

        @Override
        public Result<Void, List<DomainError>> execute(TraceContext ctx) {
            ctx.log.add(stepId + ":execute");
            return Result.failure(List.of(new DomainError(errorCode, errorCode + " message")));
        }
    }

    /** Step that throws a technical exception. */
    static class ThrowingStep implements Step<TraceContext> {
        @Override public String id() { return "THROWING"; }

        @Override
        public Result<Void, List<DomainError>> execute(TraceContext ctx) {
            throw new RuntimeException("infra down");
        }
    }

    /** Minimal concrete subclass — result = "DONE" string placed in context. */
    static class StringOrchestrator extends PipelineOrchestrator<TraceContext, String> {
        StringOrchestrator(List<Step<TraceContext>> steps) { super(steps); }

        public Result<String, List<DomainError>> execute() {
            return run(new TraceContext());
        }

        // variant for externally owned context (needed by rollback tests)
        public Result<String, List<DomainError>> execute(TraceContext ctx) {
            return run(ctx);
        }

        @Override
        protected String extractResult(TraceContext ctx) { return "DONE"; }
    }

    // ── happy path ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Happy path")
    class HappyPath {

        @Test
        @DisplayName("should execute all steps in order when all succeed")
        void shouldExecuteAllStepsInOrder() {
            var orchestrator = new StringOrchestrator(List.of(
                new OkStep("A"),
                new OkStep("B"),
                new OkStep("C")
            ));

            var ctx = new TraceContext();
            var result = orchestrator.execute(ctx);

            assertTrue(result.isSuccess());
            assertEquals("DONE", result.getOrElseThrow(e -> new RuntimeException()));
            assertEquals(List.of("A:execute", "B:execute", "C:execute"), ctx.log);
        }

        @Test
        @DisplayName("empty step list should succeed immediately")
        void emptyStepListShouldSucceed() {
            var orchestrator = new StringOrchestrator(List.of());
            assertTrue(orchestrator.execute().isSuccess());
        }
    }

    // ── failure and short-circuit ─────────────────────────────────────────

    @Nested
    @DisplayName("Failure and short-circuit")
    class FailureAndShortCircuit {

        @Test
        @DisplayName("should stop at first failing step and return its errors")
        void shouldShortCircuitOnFirstFailure() {
            var orchestrator = new StringOrchestrator(List.of(
                new OkStep("A"),
                new FailStep("B", "B_FAILED"),
                new OkStep("C")       // must NOT be reached
            ));

            var ctx = new TraceContext();
            var result = orchestrator.execute(ctx);

            assertTrue(result.isFailure());
            var errors = result.errorOrElseThrow(() -> new RuntimeException());
            assertEquals(1, errors.size());
            assertEquals("B_FAILED", errors.get(0).code());

            // C must not have been executed
            assertFalse(ctx.log.contains("C:execute"));
        }

        @Test
        @DisplayName("technical exception should be converted to Result.failure")
        void technicalExceptionShouldBecomeFailure() {
            var orchestrator = new StringOrchestrator(List.of(
                new OkStep("A"),
                new ThrowingStep()
            ));

            var result = orchestrator.execute();

            assertTrue(result.isFailure());
            var errors = result.errorOrElseThrow(() -> new RuntimeException());
            assertEquals("PIPELINE_STEP_EXCEPTION", errors.get(0).code());
            assertTrue(errors.get(0).message().contains("infra down"));
        }
    }

    // ── compensation rollback ─────────────────────────────────────────────

    @Nested
    @DisplayName("Compensation rollback (LIFO)")
    class CompensationRollback {

        @Test
        @DisplayName("should rollback COMPENSATION steps in LIFO order when pipeline fails")
        void shouldRollbackCompensationStepsInLifoOrder() {
            var ctx = new TraceContext();
            var orchestrator = new StringOrchestrator(List.of(
                new OkStep("A", RollbackStyle.COMPENSATION),
                new OkStep("B", RollbackStyle.COMPENSATION),
                new FailStep("C", "C_FAILED")
            ));

            orchestrator.execute(ctx);

            assertEquals(List.of("B:rollback", "A:rollback"), ctx.rollbackLog);
        }

        @Test
        @DisplayName("should NOT rollback NONE or FRAMEWORK_TRANSACTION steps")
        void shouldNotRollbackNoneOrFrameworkSteps() {
            var ctx = new TraceContext();
            var orchestrator = new StringOrchestrator(List.of(
                new OkStep("A", RollbackStyle.NONE),
                new OkStep("B", RollbackStyle.FRAMEWORK_TRANSACTION),
                new FailStep("C", "C_FAILED")
            ));

            orchestrator.execute(ctx);

            assertTrue(ctx.rollbackLog.isEmpty(),
                "Expected no rollback calls, got: " + ctx.rollbackLog);
        }

        @Test
        @DisplayName("should not rollback steps that came AFTER the failing step")
        void shouldNotRollbackStepsAfterFailingStep() {
            var ctx = new TraceContext();
            var orchestrator = new StringOrchestrator(List.of(
                new OkStep("A", RollbackStyle.COMPENSATION),
                new FailStep("B", "B_FAILED"),
                new OkStep("C", RollbackStyle.COMPENSATION)   // never executed
            ));

            orchestrator.execute(ctx);

            assertEquals(List.of("A:rollback"), ctx.rollbackLog);
            assertFalse(ctx.rollbackLog.contains("C:rollback"));
        }

        @Test
        @DisplayName("rollback exception must not mask original pipeline failure")
        void rollbackExceptionMustNotMaskOriginalFailure() {
            Step<TraceContext> explosiveRollback = new Step<>() {
                @Override public String id() { return "EXPLOSIVE"; }
                @Override public RollbackStyle rollbackStyle() { return RollbackStyle.COMPENSATION; }

                @Override
                public Result<Void, List<DomainError>> execute(TraceContext ctx) {
                    return Result.success(null);
                }

                @Override
                public void rollback(TraceContext ctx) {
                    throw new RuntimeException("rollback exploded");
                }
            };

            var orchestrator = new StringOrchestrator(List.of(
                explosiveRollback,
                new FailStep("FAIL", "ORIGINAL_ERROR")
            ));

            var result = orchestrator.execute();

            // original error must survive
            assertTrue(result.isFailure());
            assertEquals("ORIGINAL_ERROR",
                result.errorOrElseThrow(() -> new RuntimeException()).get(0).code());
        }
    }

    // ── constructor guard ─────────────────────────────────────────────────

    @Test
    @DisplayName("constructor should reject null steps list")
    void constructorShouldRejectNullSteps() {
        assertThrows(NullPointerException.class,
            () -> new StringOrchestrator(null));
    }
}

