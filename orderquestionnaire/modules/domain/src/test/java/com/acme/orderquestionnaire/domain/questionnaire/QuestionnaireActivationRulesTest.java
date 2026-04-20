package com.acme.orderquestionnaire.domain.questionnaire;

import com.acme.orderquestionnaire.domain.audit.OrderQuestionnaireAuditFactory;
import com.acme.orderquestionnaire.domain.question.Question;
import com.acme.orderquestionnaire.domain.question.answer.strategy.AnswerConfigurationFactory;
import com.acme.orderquestionnaire.domain.questionnaire.vo.QuestionnaireId;
import com.acme.shared.enumerator.ParameterizationStatus;
import com.acme.shared.stereotypes.test.UnitTest;
import com.acme.shared.vo.AuditInfo;
import com.acme.shared.vo.Id;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@UnitTest
@DisplayName("Questionnaire activation rules")
class QuestionnaireActivationRulesTest {

    @Test
    @DisplayName("should not be ready to activate when there are no configured questions")
    void shouldNotBeReadyWithoutConfiguredQuestions() {
        Questionnaire questionnaire = Questionnaire.rehydrate(
                QuestionnaireId.of("q_001", "APP", "JOURNEY_01"),
                "Desc",
                ParameterizationStatus.DRAFT,
                List.of(),
                audit());

        assertFalse(questionnaire.isReadyToActivate());
        assertEquals(0, questionnaire.configuredQuestions().size());
    }

    @Test
    @DisplayName("should be ready to activate when configured question has answer configuration and valid order")
    void shouldBeReadyWithValidConfiguredQuestion() {
        ConfiguredQuestion configuredQuestion = ConfiguredQuestion.createNew(
                Question.rehydrate("q1", "Q1", ParameterizationStatus.ACTIVE, "SKU", audit()),
                AnswerConfigurationFactory.createTextStrategy(),
                0);

        Questionnaire questionnaire = Questionnaire.rehydrate(
                QuestionnaireId.of("q_001", "APP", "JOURNEY_01"),
                "Desc",
                ParameterizationStatus.DRAFT,
                List.of(configuredQuestion),
                audit());

        assertTrue(questionnaire.isReadyToActivate());
        assertEquals(1, questionnaire.configuredQuestions().size());
    }

    private static AuditInfo audit() {
        return OrderQuestionnaireAuditFactory.createNew(
                Id.withId("11111111-1111-1111-1111-111111111111"),
                "REF",
                "Tester",
                "tester@acme.com",
                LocalDateTime.parse("2026-01-01T10:00:00"))
                .getOrElseThrow(error -> new IllegalStateException("Invalid audit test data: " + error));
    }
}

