package com.acme.orderquestionnaire.application.questionnaire.service.step;

import com.acme.orderquestionnaire.application.audit.dto.command.AuditUserParam;
import com.acme.orderquestionnaire.application.questionnaire.dto.command.CreateQuestionnaireCommand;
import com.acme.orderquestionnaire.application.questionnaire.port.out.repository.QuestionnaireCommandOutPort;
import com.acme.orderquestionnaire.application.questionnaire.service.context.CreateQuestionnairePipelineContext;
import com.acme.orderquestionnaire.domain.audit.OrderQuestionnaireAuditFactory;
import com.acme.orderquestionnaire.domain.questionnaire.Questionnaire;
import com.acme.orderquestionnaire.domain.questionnaire.QuestionnaireFactory;
import com.acme.orderquestionnaire.domain.questionnaire.vo.QuestionnaireId;
import com.acme.shared.enumerator.ParameterizationStatus;
import com.acme.shared.pattern.pipeline.RollbackStyle;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;
import com.acme.shared.stereotypes.test.UnitTest;
import com.acme.shared.vo.AuditInfo;
import com.acme.shared.vo.Id;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@UnitTest
@DisplayName("BuildAndPersistQuestionnaireStep")
class BuildAndPersistQuestionnaireStepTest {

    private static final LocalDateTime FIXED_DATE = LocalDateTime.parse("2026-01-01T10:00:00");

    @Test
    @DisplayName("should report FRAMEWORK_TRANSACTION rollback style when configured")
    void shouldHaveConfiguredRollbackStyle() {
        BuildAndPersistQuestionnaireStep step = new BuildAndPersistQuestionnaireStep(
                mock(QuestionnaireCommandOutPort.class), RollbackStyle.FRAMEWORK_TRANSACTION);
        assertEquals(RollbackStyle.FRAMEWORK_TRANSACTION, step.rollbackStyle());
    }

    @Test
    @DisplayName("should report COMPENSATION rollback style when configured")
    void shouldHaveCompensationRollbackStyle() {
        BuildAndPersistQuestionnaireStep step = new BuildAndPersistQuestionnaireStep(
                mock(QuestionnaireCommandOutPort.class), RollbackStyle.COMPENSATION);
        assertEquals(RollbackStyle.COMPENSATION, step.rollbackStyle());
    }

    @Test
    @DisplayName("should build, persist, and store questionnaire and createdView in context on success")
    void shouldBuildAndPersistQuestionnaireAndStoreInContext() {
        QuestionnaireCommandOutPort outPort = mock(QuestionnaireCommandOutPort.class);
        Questionnaire persisted = buildQuestionnaire();
        when(outPort.create(any())).thenReturn(Result.success(persisted));

        CreateQuestionnairePipelineContext context = contextWithAudit();
        BuildAndPersistQuestionnaireStep step = new BuildAndPersistQuestionnaireStep(outPort, RollbackStyle.COMPENSATION);

        Result<Void, List<DomainError>> result = step.execute(context);

        assertInstanceOf(Result.Success.class, result);
        assertNotNull(context.questionnaire());
        assertNotNull(context.createdView());
    }

    @Test
    @DisplayName("should propagate port failure without storing questionnaire in context")
    void shouldPropagatePortFailure() {
        QuestionnaireCommandOutPort outPort = mock(QuestionnaireCommandOutPort.class);
        when(outPort.create(any())).thenReturn(Result.failure(
                List.of(new DomainError("PERSIST_ERROR", "persistence failed"))));

        CreateQuestionnairePipelineContext context = contextWithAudit();
        BuildAndPersistQuestionnaireStep step = new BuildAndPersistQuestionnaireStep(outPort, RollbackStyle.COMPENSATION);

        Result<Void, List<DomainError>> result = step.execute(context);

        assertInstanceOf(Result.Failure.class, result);
    }

    @Test
    @DisplayName("rollback should call deleteById when COMPENSATION and questionnaire is stored in context")
    void shouldCallDeleteOnRollbackWithCompensation() {
        QuestionnaireCommandOutPort outPort = mock(QuestionnaireCommandOutPort.class);
        Questionnaire persisted = buildQuestionnaire();
        when(outPort.create(any())).thenReturn(Result.success(persisted));
        when(outPort.deleteById(any())).thenReturn(Result.success(null));

        BuildAndPersistQuestionnaireStep step = new BuildAndPersistQuestionnaireStep(outPort, RollbackStyle.COMPENSATION);
        CreateQuestionnairePipelineContext context = contextWithAudit();
        step.execute(context);

        step.rollback(context);

        verify(outPort).deleteById(QuestionnaireId.of("q_001", "APP", "JOURNEY_01"));
    }

    @Test
    @DisplayName("rollback should be a no-op when FRAMEWORK_TRANSACTION")
    void shouldSkipRollbackWhenFrameworkTransaction() {
        QuestionnaireCommandOutPort outPort = mock(QuestionnaireCommandOutPort.class);
        BuildAndPersistQuestionnaireStep step = new BuildAndPersistQuestionnaireStep(outPort, RollbackStyle.FRAMEWORK_TRANSACTION);
        CreateQuestionnairePipelineContext context = contextWithAudit();

        step.rollback(context);

        verify(outPort, never()).deleteById(any());
    }

    @Test
    @DisplayName("rollback should be a no-op when COMPENSATION but no questionnaire in context")
    void shouldSkipRollbackWhenNoQuestionnaireInContext() {
        QuestionnaireCommandOutPort outPort = mock(QuestionnaireCommandOutPort.class);
        BuildAndPersistQuestionnaireStep step = new BuildAndPersistQuestionnaireStep(outPort, RollbackStyle.COMPENSATION);
        CreateQuestionnairePipelineContext context = contextWithAudit();

        step.rollback(context); // nothing was persisted

        verify(outPort, never()).deleteById(any());
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private static CreateQuestionnairePipelineContext contextWithAudit() {
        var cmd = new CreateQuestionnaireCommand("q_001", "APP", "JOURNEY_01", "Description",
                new AuditUserParam(Id.withId("00000000-0000-0000-0000-000000000001"), "REF-1", "User", "u@acme.com"),
                FIXED_DATE);
        CreateQuestionnairePipelineContext context = new CreateQuestionnairePipelineContext(cmd);
        context.auditInfo(auditInfo());
        return context;
    }

    private static AuditInfo auditInfo() {
        return OrderQuestionnaireAuditFactory.createNew(
                Id.withId("00000000-0000-0000-0000-000000000001"), "REF-1", "User", "u@acme.com", FIXED_DATE
        ).getOrElseThrow(e -> new IllegalStateException("Invalid audit: " + e));
    }

    private static Questionnaire buildQuestionnaire() {
        return QuestionnaireFactory.rehydrate("q_001", "APP", "JOURNEY_01", "Description",
                        ParameterizationStatus.DRAFT, auditInfo())
                .flatMap(QuestionnaireFactory.QuestionnaireBuilder::build)
                .getOrElseThrow(e -> new IllegalStateException("Invalid questionnaire: " + e));
    }
}

