package com.acme.shared.pattern.pipeline;

import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Objects;

/**
 * Generic engine for executing a sequence of ordered {@link Step}s against a shared
 * {@link PipelineContext}, with automatic LIFO compensation rollback on failure.
 *
 * <h3>Execution model</h3>
 * <ol>
 *   <li>Steps are executed in list order.</li>
 *   <li>On the first {@code Result.failure}, execution stops (<em>short-circuit</em>).</li>
 *   <li>Technical exceptions inside steps are caught and converted to {@code Result.failure}.</li>
 *   <li>Steps with {@link RollbackStyle#COMPENSATION} that already completed are rolled back
 *       in LIFO order (Saga pattern).</li>
 *   <li>Steps with {@link RollbackStyle#FRAMEWORK_TRANSACTION} are skipped during rollback —
 *       the framework (e.g. Spring {@code @Transactional}) handles DB rollback via the
 *       {@link com.acme.shared.exception.PipelineFailureException} that the transactional
 *       boundary should throw.</li>
 * </ol>
 *
 * <h3>How to use</h3>
 * Subclass this orchestrator for each use-case service:
 * <pre>{@code
 * public class CreateQuestionnaireService
 *         extends PipelineOrchestrator<CreateQuestionnairePipelineContext, QuestionnaireCreatedView>
 *         implements CreateQuestionnaireUseCase {
 *
 *     public CreateQuestionnaireService(List<Step<CreateQuestionnairePipelineContext>> steps) {
 *         super(steps);
 *     }
 *
 *     @Override
 *     public Result<QuestionnaireCreatedView, List<DomainError>> execute(CreateQuestionnaireCommand cmd) {
 *         return run(new CreateQuestionnairePipelineContext(cmd));
 *     }
 *
 *     @Override
 *     protected QuestionnaireCreatedView extractResult(CreateQuestionnairePipelineContext ctx) {
 *         return ctx.createdView();
 *     }
 * }
 * }</pre>
 *
 * @param <C> context type — must extend {@link PipelineContext}
 * @param <V> value type returned on success by the use-case port
 */
public abstract class PipelineOrchestrator<C extends PipelineContext, V> {

    private final List<Step<C>> steps;

    protected PipelineOrchestrator(List<Step<C>> steps) {
        this.steps = List.copyOf(Objects.requireNonNull(steps, "steps must not be null"));
    }

    /**
     * Executes the pipeline against the given context.
     *
     * @param context pre-built context containing the command/input
     * @return {@code Result.success(extractResult(context))} when all steps succeed, or
     *         {@code Result.failure(errors)} from the first failing step (after compensation rollback)
     */
    protected final Result<V, List<DomainError>> run(C context) {
        Deque<Step<C>> compensatable = new ArrayDeque<>();

        for (Step<C> step : steps) {
            Result<Void, List<DomainError>> stepResult = safeExecute(step, context);

            if (stepResult.isFailure()) {
                compensate(compensatable, context);

                return Result.failure(
                    stepResult.errorOrElseThrow(
                        () -> new IllegalStateException("Expected failure but no error present in step: " + step.id())
                    )
                );
            }

            if (step.rollbackStyle() == RollbackStyle.COMPENSATION) {
                compensatable.push(step);
            }
        }

        return Result.success(extractResult(context));
    }

    /**
     * Extracts the final use-case result value from the context after all steps succeed.
     * Subclasses define what to read from the context (e.g. {@code context.createdView()}).
     */
    protected abstract V extractResult(C context);

    // ── internals ─────────────────────────────────────────────────────────

    private Result<Void, List<DomainError>> safeExecute(Step<C> step, C context) {
        try {
            return step.execute(context);
        } catch (Exception ex) {
            return Result.failure(List.of(new DomainError(
                "PIPELINE_STEP_EXCEPTION",
                "Step [%s] threw a technical exception: %s".formatted(step.id(), ex.getMessage())
            )));
        }
    }

    private void compensate(Deque<Step<C>> compensatable, C context) {
        while (!compensatable.isEmpty()) {
            Step<C> step = compensatable.pop();
            try {
                step.rollback(context);
            } catch (Exception ignored) {
                // compensation failures must not mask the original pipeline failure
            }
        }
    }
}

