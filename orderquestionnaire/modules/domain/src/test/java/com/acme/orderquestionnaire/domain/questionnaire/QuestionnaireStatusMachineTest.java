package com.acme.orderquestionnaire.domain.questionnaire;

import com.acme.orderquestionnaire.domain.audit.OrderQuestionnaireAuditFactory;
import com.acme.orderquestionnaire.domain.question.Question;
import com.acme.orderquestionnaire.domain.questionnaire.answer.strategy.AnswerConfigurationFactory;
import com.acme.orderquestionnaire.domain.questionnaire.vo.QuestionnaireId;
import com.acme.shared.engine.state.TransitionResult;
import com.acme.shared.enumerator.ParameterizationStatus;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;
import com.acme.shared.stereotypes.test.UnitTest;
import com.acme.shared.vo.AuditInfo;
import com.acme.shared.vo.Id;
import com.acme.shared.vo.AuditUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

@UnitTest
@DisplayName("QuestionnaireStatusMachine")
class QuestionnaireStatusMachineTest {

    @Test
    @DisplayName("should transition from draft to inactive and update audit")
    void shouldTransitionDraftToInactive() {
        Questionnaire questionnaire = questionnaire(ParameterizationStatus.DRAFT, List.of());
        QuestionnaireStatusTransitionContext context = contextFor(questionnaire);

        Result<TransitionResult<ParameterizationStatus, QuestionnaireStatusTransitionContext>, List<DomainError>> result =
                QuestionnaireStatusMachine.transition(ParameterizationStatus.DRAFT, ParameterizationStatus.INACTIVE, context);

        assertInstanceOf(Result.Success.class, result);
        TransitionResult<ParameterizationStatus, QuestionnaireStatusTransitionContext> transition =
                result.getOrElseThrow(error ->
                        new IllegalStateException("Expected success but got failure: " + error));
        assertEquals(ParameterizationStatus.INACTIVE, transition.targetState());
        assertTrue(transition.context().statusChanged());
        assertEquals(updatedAt(), transition.context().auditInfo().updatedAt());
    }

    @Test
    @DisplayName("should transition from draft to active when questionnaire is activation-ready")
    void shouldTransitionDraftToActiveWhenReady() {
        ConfiguredQuestion configured = ConfiguredQuestion.createNew(
                question("q1"),
                AnswerConfigurationFactory.createTextStrategy(),
                1);
        Questionnaire questionnaire = questionnaire(ParameterizationStatus.DRAFT, List.of(configured));

        Result<TransitionResult<ParameterizationStatus, QuestionnaireStatusTransitionContext>, List<DomainError>> result =
                QuestionnaireStatusMachine.transition(
                        ParameterizationStatus.DRAFT,
                        ParameterizationStatus.ACTIVE,
                        contextFor(questionnaire));

        assertInstanceOf(Result.Success.class, result);
        assertEquals(ParameterizationStatus.ACTIVE,
                result.getOrElseThrow(error ->
                        new IllegalStateException("Expected success but got failure: " + error)).targetState());
    }

    @Test
    @DisplayName("should fail transition to active when questionnaire is not activation-ready")
    void shouldFailActivationWhenNotReady() {
        Questionnaire questionnaire = questionnaire(ParameterizationStatus.DRAFT, List.of());

        Result<TransitionResult<ParameterizationStatus, QuestionnaireStatusTransitionContext>, List<DomainError>> result =
                QuestionnaireStatusMachine.transition(
                        ParameterizationStatus.DRAFT,
                        ParameterizationStatus.ACTIVE,
                        contextFor(questionnaire));

        assertInstanceOf(Result.Failure.class, result);
        List<DomainError> errors = result.errorOrElseThrow(() ->
                new IllegalStateException("Expected failure but got success"));
        assertTrue(errors.stream().anyMatch(error -> error.code().equals("INVALID_CONFIGURED_QUESTION_FOR_ACTIVATION")));
    }

    @Test
    @DisplayName("should fail transition when metadata is missing")
    void shouldFailWhenMetadataMissing() {
        Questionnaire questionnaire = questionnaire(ParameterizationStatus.DRAFT, List.of());
        QuestionnaireStatusTransitionContext invalid = new QuestionnaireStatusTransitionContext(
                questionnaire,
                null,
                null,
                questionnaire.auditInfo(),
                false);

        Result<TransitionResult<ParameterizationStatus, QuestionnaireStatusTransitionContext>, List<DomainError>> result =
                QuestionnaireStatusMachine.transition(
                        ParameterizationStatus.DRAFT,
                        ParameterizationStatus.INACTIVE,
                        invalid);

        assertInstanceOf(Result.Failure.class, result);
        List<DomainError> errors = result.errorOrElseThrow(() ->
                new IllegalStateException("Expected failure but got success"));
        assertTrue(errors.stream().anyMatch(error -> error.code().equals("TRANSITION_GUARD_VIOLATION")));
    }

    private static QuestionnaireStatusTransitionContext contextFor(Questionnaire questionnaire) {
        AuditUser actor = OrderQuestionnaireAuditFactory.user(
                Id.withId("33333333-3333-3333-3333-333333333333"),
                "REF-UPD",
                "Updater",
                "updater@acme.com")
                .getOrElseThrow(error -> new IllegalStateException("Invalid actor test data: " + error));
        AuditInfo auditInfo = questionnaire.auditInfo().withUpdate(actor, updatedAt());
        return new QuestionnaireStatusTransitionContext(questionnaire, actor, updatedAt(), auditInfo, false);
    }

    private static Questionnaire questionnaire(ParameterizationStatus status, List<ConfiguredQuestion> questions) {
        return Questionnaire.rehydrate(
                QuestionnaireId.of("q_001", "APP", "JOURNEY_01"),
                "Description",
                status,
                questions,
                createdAudit());
    }

    private static Question question(String id) {
        return Question.rehydrate(id, "Label " + id, ParameterizationStatus.ACTIVE, "SKU", createdAudit());
    }

    private static AuditInfo createdAudit() {
        return OrderQuestionnaireAuditFactory.createNew(
                Id.withId("11111111-1111-1111-1111-111111111111"),
                "REF-CREATE",
                "Creator",
                "creator@acme.com",
                LocalDateTime.parse("2026-01-01T08:00:00"))
                .getOrElseThrow(error -> new IllegalStateException("Invalid audit test data: " + error));
    }

    private static LocalDateTime updatedAt() {
        return LocalDateTime.parse("2026-01-01T09:00:00");
    }
}

