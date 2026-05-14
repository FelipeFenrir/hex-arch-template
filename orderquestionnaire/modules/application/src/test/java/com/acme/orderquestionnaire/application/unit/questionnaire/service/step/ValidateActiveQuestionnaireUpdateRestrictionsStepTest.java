package com.acme.orderquestionnaire.application.unit.questionnaire.service.step;

import com.acme.orderquestionnaire.application.questionnaire.dto.command.UpdateQuestionnaireCommand;
import com.acme.orderquestionnaire.application.questionnaire.service.context.UpdateQuestionnairePipelineContext;
import com.acme.orderquestionnaire.application.questionnaire.service.step.ValidateActiveQuestionnaireUpdateRestrictionsStep;
import com.acme.orderquestionnaire.domain.audit.OrderQuestionnaireAuditFactory;
import com.acme.orderquestionnaire.domain.questionnaire.Questionnaire;
import com.acme.shared.enumerator.ParameterizationStatus;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;
import com.acme.shared.stereotypes.test.UnitTest;
import com.acme.shared.vo.Id;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

@UnitTest
@DisplayName("ValidateActiveQuestionnaireUpdateRestrictionsStep")
class ValidateActiveQuestionnaireUpdateRestrictionsStepTest {

    private static final LocalDateTime FIXED_DATE = LocalDateTime.parse("2026-01-02T10:00:00");
    private final ValidateActiveQuestionnaireUpdateRestrictionsStep step = new ValidateActiveQuestionnaireUpdateRestrictionsStep();

    @Test
    @DisplayName("should succeed when existing questionnaire is not ACTIVE")
    void shouldSucceedWhenQuestionnaireIsNotActive() {
        UpdateQuestionnairePipelineContext context = context(
                questionnaireWithStatus(ParameterizationStatus.DRAFT),
                command(null, null, null)
        );

        Result<Void, List<DomainError>> result = step.execute(context);

        assertInstanceOf(Result.Success.class, result);
    }

    @Test
    @DisplayName("should succeed when ACTIVE and command only deactivates")
    void shouldSucceedWhenActiveAndOnlyDeactivates() {
        UpdateQuestionnairePipelineContext context = context(
                questionnaireWithStatus(ParameterizationStatus.ACTIVE),
                command(ParameterizationStatus.INACTIVE, null, null)
        );

        Result<Void, List<DomainError>> result = step.execute(context);

        assertInstanceOf(Result.Success.class, result);
    }

    @Test
    @DisplayName("should fail when ACTIVE and description changes")
    void shouldFailWhenActiveAndDescriptionChanges() {
        UpdateQuestionnairePipelineContext context = context(
                questionnaireWithStatus(ParameterizationStatus.ACTIVE),
                command(null, "new description", null)
        );

        Result<Void, List<DomainError>> result = step.execute(context);

        assertInstanceOf(Result.Failure.class, result);
        List<DomainError> errors = result.errorOrElseThrow(() -> new IllegalStateException("Expected failure"));
        assertTrue(errors.stream().anyMatch(e -> e.code().equals("QUESTIONNAIRE_UPDATE_NOT_ALLOWED")));
    }

    @Test
    @DisplayName("should fail when ACTIVE and has structural changes")
    void shouldFailWhenActiveAndHasStructuralChanges() {
        UpdateQuestionnairePipelineContext context = context(
                questionnaireWithStatus(ParameterizationStatus.ACTIVE),
                command(null, null, List.of("q_remove"))
        );

        Result<Void, List<DomainError>> result = step.execute(context);

        assertInstanceOf(Result.Failure.class, result);
        List<DomainError> errors = result.errorOrElseThrow(() -> new IllegalStateException("Expected failure"));
        assertTrue(errors.stream().anyMatch(e -> e.code().equals("QUESTIONNAIRE_UPDATE_NOT_ALLOWED")));
    }

    private static UpdateQuestionnairePipelineContext context(Questionnaire existing, UpdateQuestionnaireCommand cmd) {
        UpdateQuestionnairePipelineContext context = new UpdateQuestionnairePipelineContext(cmd);
        context.existingQuestionnaire(existing);
        return context;
    }

    private static UpdateQuestionnaireCommand command(ParameterizationStatus status, String description, List<String> idsToRemove) {
        return new UpdateQuestionnaireCommand(
                "q_001",
                "APP",
                "JOURNEY_01",
                description,
                status,
                null,
                idsToRemove,
                null,
                FIXED_DATE
        );
    }

    private static Questionnaire questionnaireWithStatus(ParameterizationStatus status) {
        var auditInfo = OrderQuestionnaireAuditFactory.createNew(
                Id.withId("00000000-0000-0000-0000-000000000001"),
                "REF-1",
                "User",
                "u@acme.com",
                LocalDateTime.parse("2026-01-01T10:00:00")
        ).getOrElseThrow(e -> new IllegalStateException("Invalid audit: " + e));

        return Questionnaire.rehydrate("q_001", "APP", "JOURNEY_01", "current description", status, auditInfo);
    }
}

