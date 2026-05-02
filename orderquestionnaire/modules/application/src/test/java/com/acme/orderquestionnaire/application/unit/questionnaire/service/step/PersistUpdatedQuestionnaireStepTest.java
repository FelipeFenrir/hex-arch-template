package com.acme.orderquestionnaire.application.unit.questionnaire.service.step;

import com.acme.orderquestionnaire.application.questionnaire.dto.command.UpdateQuestionnaireCommand;
import com.acme.orderquestionnaire.application.questionnaire.port.out.repository.QuestionnaireCommandOutPort;
import com.acme.orderquestionnaire.application.questionnaire.service.context.UpdateQuestionnairePipelineContext;
import com.acme.orderquestionnaire.application.questionnaire.service.step.PersistUpdatedQuestionnaireStep;
import com.acme.orderquestionnaire.domain.audit.OrderQuestionnaireAuditFactory;
import com.acme.orderquestionnaire.domain.questionnaire.Questionnaire;
import com.acme.orderquestionnaire.domain.questionnaire.QuestionnaireFactory;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@UnitTest
@DisplayName("PersistUpdatedQuestionnaireStep")
class PersistUpdatedQuestionnaireStepTest {

    private static final LocalDateTime FIXED_DATE = LocalDateTime.parse("2026-01-01T10:00:00");

    @Test
    @DisplayName("should always use FRAMEWORK_TRANSACTION rollback style")
    void shouldUseFrameworkTransactionRollbackStyle() {
        PersistUpdatedQuestionnaireStep step = new PersistUpdatedQuestionnaireStep(
                mock(QuestionnaireCommandOutPort.class));
        assertEquals(RollbackStyle.FRAMEWORK_TRANSACTION, step.rollbackStyle());
    }

    @Test
    @DisplayName("should persist updated questionnaire and store view in context")
    void shouldPersistAndStoreUpdatedView() {
        QuestionnaireCommandOutPort outPort = mock(QuestionnaireCommandOutPort.class);
        Questionnaire updated = buildQuestionnaire();
        when(outPort.update(any())).thenReturn(Result.success(updated));

        PersistUpdatedQuestionnaireStep step = new PersistUpdatedQuestionnaireStep(outPort);
        UpdateQuestionnairePipelineContext context = contextWithUpdatedQuestionnaire(updated);

        Result<Void, List<DomainError>> result = step.execute(context);

        assertInstanceOf(Result.Success.class, result);
        assertNotNull(context.updatedView());
    }

    @Test
    @DisplayName("should propagate port failure")
    void shouldPropagatePortFailure() {
        QuestionnaireCommandOutPort outPort = mock(QuestionnaireCommandOutPort.class);
        when(outPort.update(any())).thenReturn(Result.failure(
                List.of(new DomainError("PERSIST_ERROR", "update failed"))));

        Questionnaire questionnaire = buildQuestionnaire();
        PersistUpdatedQuestionnaireStep step = new PersistUpdatedQuestionnaireStep(outPort);
        UpdateQuestionnairePipelineContext context = contextWithUpdatedQuestionnaire(questionnaire);

        Result<Void, List<DomainError>> result = step.execute(context);

        assertInstanceOf(Result.Failure.class, result);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private static UpdateQuestionnairePipelineContext contextWithUpdatedQuestionnaire(Questionnaire questionnaire) {
        var cmd = new UpdateQuestionnaireCommand("q_001", "APP", "JOURNEY_01", "Description",
                null, null, null, null, FIXED_DATE);
        UpdateQuestionnairePipelineContext context = new UpdateQuestionnairePipelineContext(cmd);
        context.updatedQuestionnaire(questionnaire);
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

