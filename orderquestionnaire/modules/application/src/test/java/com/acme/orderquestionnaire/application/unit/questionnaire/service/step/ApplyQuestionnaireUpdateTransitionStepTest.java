package com.acme.orderquestionnaire.application.unit.questionnaire.service.step;

import com.acme.orderquestionnaire.application.audit.dto.command.AuditUserParam;
import com.acme.orderquestionnaire.application.questionnaire.dto.command.UpdateQuestionnaireCommand;
import com.acme.orderquestionnaire.application.questionnaire.service.context.UpdateQuestionnairePipelineContext;
import com.acme.orderquestionnaire.application.questionnaire.service.step.ApplyQuestionnaireUpdateTransitionStep;
import com.acme.orderquestionnaire.domain.audit.OrderQuestionnaireAuditFactory;
import com.acme.orderquestionnaire.domain.question.Question;
import com.acme.orderquestionnaire.domain.questionnaire.ConfiguredQuestion;
import com.acme.orderquestionnaire.domain.questionnaire.Questionnaire;
import com.acme.orderquestionnaire.domain.questionnaire.answer.strategy.AnswerConfigurationFactory;
import com.acme.shared.enumerator.ParameterizationStatus;
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
import static org.junit.jupiter.api.Assertions.assertTrue;

@UnitTest
@DisplayName("ApplyQuestionnaireUpdateTransitionStep")
class ApplyQuestionnaireUpdateTransitionStepTest {

    private static final LocalDateTime FIXED_DATE = LocalDateTime.parse("2026-01-02T10:00:00");
    private final ApplyQuestionnaireUpdateTransitionStep step = new ApplyQuestionnaireUpdateTransitionStep();

    @Test
    @DisplayName("should fail when resolved description is blank")
    void shouldFailWhenResolvedDescriptionIsBlank() {
        Questionnaire existing = questionnaire(ParameterizationStatus.DRAFT, "current", List.of());
        UpdateQuestionnairePipelineContext context = context(existing, command(" ", null));
        context.updatedActor(actor());
        context.configuredQuestions(List.of());

        Result<Void, List<DomainError>> result = step.execute(context);

        assertInstanceOf(Result.Failure.class, result);
        List<DomainError> errors = result.errorOrElseThrow(() -> new IllegalStateException("Expected failure"));
        assertTrue(errors.stream().anyMatch(e -> e.code().equals("INVALID_DESCRIPTION")));
    }

    @Test
    @DisplayName("should apply valid transition and store updated questionnaire in context")
    void shouldApplyTransitionAndStoreUpdatedQuestionnaire() {
        ConfiguredQuestion configured = configuredQuestion("q_01");
        Questionnaire existing = questionnaire(ParameterizationStatus.DRAFT, "old description", List.of(configured));

        UpdateQuestionnairePipelineContext context = context(existing, command("new description", ParameterizationStatus.INACTIVE));
        context.updatedActor(actor());
        context.configuredQuestions(List.of(configured));

        Result<Void, List<DomainError>> result = step.execute(context);

        assertInstanceOf(Result.Success.class, result);
        assertNotNull(context.updatedQuestionnaire());
        assertEquals(ParameterizationStatus.INACTIVE, context.updatedQuestionnaire().status());
        assertEquals("new description", context.updatedQuestionnaire().description());
    }

    @Test
    @DisplayName("should fail when requested status transition is invalid")
    void shouldFailWhenStatusTransitionIsInvalid() {
        Questionnaire existing = questionnaire(ParameterizationStatus.INACTIVE, "desc", List.of());
        UpdateQuestionnairePipelineContext context = context(existing, command("updated", ParameterizationStatus.DRAFT));
        context.updatedActor(actor());
        context.configuredQuestions(List.of());

        Result<Void, List<DomainError>> result = step.execute(context);

        assertInstanceOf(Result.Failure.class, result);
        List<DomainError> errors = result.errorOrElseThrow(() -> new IllegalStateException("Expected failure"));
        assertTrue(errors.stream().anyMatch(e -> e.code().equals("INVALID_STATUS_TRANSITION")));
    }

    private static UpdateQuestionnairePipelineContext context(Questionnaire existing, UpdateQuestionnaireCommand command) {
        UpdateQuestionnairePipelineContext context = new UpdateQuestionnairePipelineContext(command);
        context.existingQuestionnaire(existing);
        return context;
    }

    private static UpdateQuestionnaireCommand command(String description, ParameterizationStatus desiredStatus) {
        return new UpdateQuestionnaireCommand(
                "q_001",
                "APP",
                "JOURNEY_01",
                description,
                desiredStatus,
                null,
                null,
                new AuditUserParam(Id.withId("00000000-0000-0000-0000-000000000002"), "REF-2", "Updater", "upd@acme.com"),
                FIXED_DATE
        );
    }

    private static AuditInfo auditInfo() {
        return OrderQuestionnaireAuditFactory.createNew(
                Id.withId("00000000-0000-0000-0000-000000000001"),
                "REF-1",
                "User",
                "u@acme.com",
                LocalDateTime.parse("2026-01-01T10:00:00")
        ).getOrElseThrow(e -> new IllegalStateException("Invalid audit: " + e));
    }

    private static Questionnaire questionnaire(ParameterizationStatus status, String description, List<ConfiguredQuestion> configuredQuestions) {
        return Questionnaire.rehydrate("q_001", "APP", "JOURNEY_01", description, status, configuredQuestions, auditInfo());
    }

    private static ConfiguredQuestion configuredQuestion(String questionId) {
        Question question = Question.rehydrate(questionId, "Label " + questionId, ParameterizationStatus.ACTIVE, "SKU-1", auditInfo());
        return ConfiguredQuestion.createNew(question, AnswerConfigurationFactory.createTextStrategy(), 1);
    }

    private static com.acme.shared.vo.AuditUser actor() {
        return OrderQuestionnaireAuditFactory.user(
                Id.withId("00000000-0000-0000-0000-000000000002"),
                "REF-2",
                "Updater",
                "upd@acme.com"
        ).getOrElseThrow(e -> new IllegalStateException("Invalid actor: " + e));
    }
}

