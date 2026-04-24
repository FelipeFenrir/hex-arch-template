package com.acme.orderquestionnaire.domain.questionnaire;

import com.acme.orderquestionnaire.domain.audit.OrderQuestionnaireAuditFactory;
import com.acme.orderquestionnaire.domain.question.Question;
import com.acme.orderquestionnaire.domain.questionnaire.answer.strategy.AnswerConfigurationFactory;
import com.acme.orderquestionnaire.domain.questionnaire.vo.QuestionnaireId;
import com.acme.shared.pattern.state.TransitionResult;
import com.acme.shared.enumerator.ParameterizationStatus;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;
import com.acme.shared.stereotypes.test.UnitTest;
import com.acme.shared.vo.AuditInfo;
import com.acme.shared.vo.AuditUser;
import com.acme.shared.vo.Id;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

@UnitTest
@DisplayName("QuestionnaireStatusMachine mutation")
class QuestionnaireStatusMachineMutationTest {

    @Test
    @DisplayName("activation guard must reject questionnaire without configured question")
    void activationGuardMustRejectWithoutConfiguredQuestion() {
        Questionnaire questionnaire = Questionnaire.rehydrate(
                QuestionnaireId.of("q_001", "APP", "JOURNEY_01"),
                "Desc",
                ParameterizationStatus.DRAFT,
                List.of(),
                audit());

        Result<TransitionResult<ParameterizationStatus, QuestionnaireStatusTransitionContext>, List<DomainError>> result =
                QuestionnaireStatusMachine.transition(
                        ParameterizationStatus.DRAFT,
                        ParameterizationStatus.ACTIVE,
                        contextFor(questionnaire));

        assertInstanceOf(Result.Failure.class, result);
        assertTrue(result.errorOrElseThrow(() ->
                new IllegalStateException("Expected failure but got success")).stream()
                .anyMatch(error -> error.code().equals("INVALID_CONFIGURED_QUESTION_FOR_ACTIVATION")));
    }

    @Test
    @DisplayName("activation guard must allow questionnaire with valid configured question")
    void activationGuardMustAllowWithConfiguredQuestion() {
        Question q1 = Question.rehydrate("q1", "Q1", ParameterizationStatus.ACTIVE, "SKU", audit());
        ConfiguredQuestion configuredQuestion = ConfiguredQuestion.createNew(
                q1,
                AnswerConfigurationFactory.createTextStrategy(),
                1);

        Questionnaire questionnaire = Questionnaire.rehydrate(
                QuestionnaireId.of("q_001", "APP", "JOURNEY_01"),
                "Desc",
                ParameterizationStatus.DRAFT,
                List.of(configuredQuestion),
                audit());

        Result<TransitionResult<ParameterizationStatus, QuestionnaireStatusTransitionContext>, List<DomainError>> result =
                QuestionnaireStatusMachine.transition(
                        ParameterizationStatus.DRAFT,
                        ParameterizationStatus.ACTIVE,
                        contextFor(questionnaire));

        assertInstanceOf(Result.Success.class, result);
    }

    private static QuestionnaireStatusTransitionContext contextFor(Questionnaire questionnaire) {
        AuditUser actor = OrderQuestionnaireAuditFactory.user(
                Id.withId("22222222-2222-2222-2222-222222222222"),
                "REF-UPD",
                "Updater",
                "updater@acme.com")
                .getOrElseThrow(error -> new IllegalStateException("Invalid actor test data: " + error));
        return new QuestionnaireStatusTransitionContext(
                questionnaire,
                actor,
                LocalDateTime.parse("2026-01-01T11:00:00"),
                questionnaire.auditInfo().withUpdate(actor, LocalDateTime.parse("2026-01-01T11:00:00")),
                false);
    }

    private static AuditInfo audit() {
        return OrderQuestionnaireAuditFactory.createNew(
                Id.withId("11111111-1111-1111-1111-111111111111"),
                "REF",
                "Creator",
                "creator@acme.com",
                LocalDateTime.parse("2026-01-01T10:00:00"))
                .getOrElseThrow(error -> new IllegalStateException("Invalid audit test data: " + error));
    }
}

