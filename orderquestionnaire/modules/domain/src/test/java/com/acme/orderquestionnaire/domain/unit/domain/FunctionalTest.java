package com.acme.orderquestionnaire.domain.unit.domain;

import com.acme.orderquestionnaire.domain.question.QuestionFactory;
import com.acme.orderquestionnaire.domain.questionnaire.ConfiguredQuestionFactory;
import com.acme.orderquestionnaire.domain.questionnaire.QuestionValidationFailure;
import com.acme.orderquestionnaire.domain.questionnaire.QuestionnaireFactory;
import com.acme.orderquestionnaire.domain.questionnaire.answer.AnswerOptionComposer;
import com.acme.orderquestionnaire.domain.questionnaire.conditioner.NumericCondition;
import com.acme.orderquestionnaire.domain.questionnaire.conditioner.QuestionConditionComposer;
import com.acme.orderquestionnaire.domain.questionnaire.tree.QuestionnaireTree;
import com.acme.orderquestionnaire.domain.unit.testutils.mocks.audit.AuditTestData;
import com.acme.shared.enumerator.ParameterizationStatus;
import com.acme.shared.stereotypes.test.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@UnitTest
@DisplayName("Functional Test: Building and Validating a Complete Questionnaire")
class FunctionalTest {

    @Test
    @DisplayName("When building complete questionnaire fluently with QuestionnaireFactory")
    void shouldBuildCompleteQuestionnaireWithQuestionnaireFactory() {
        var question1 = QuestionFactory
                .rehydrate("q_one", "How satisfied are you?", ParameterizationStatus.ACTIVE, "SALE", AuditTestData.createdAudit())
                .flatMap(builder -> builder.build())
                .getOrElseThrow(error -> new IllegalStateException("Expected success but got failure: " + error));

        var question2 = QuestionFactory
                .rehydrate("q_two", "Would you recommend us?", ParameterizationStatus.ACTIVE, "SALE", AuditTestData.createdAudit())
                .flatMap(builder -> builder.build())
                .getOrElseThrow(error -> new IllegalStateException("Expected success but got failure: " + error));

        var configuredQuestion1 = ConfiguredQuestionFactory.from(question1)
                .flatMap(ConfiguredQuestionFactory.ConfiguredQuestionBuilder::asNumber)
                .getOrElseThrow(error -> new IllegalStateException("Expected success but got failure: " + error));

        var configuredQuestion2 = ConfiguredQuestionFactory.from(question2)
                .flatMap(builder -> builder
                        .withCondition(QuestionConditionComposer.condition(new NumericCondition("q_one", 7, ">")).build())
                        .asOptionList(AnswerOptionComposer.options(
                                AnswerOptionComposer.option("yes", "Yes"),
                                AnswerOptionComposer.option("no", "No"),
                                AnswerOptionComposer.option("maybe", "Maybe")
                        )))
                .getOrElseThrow(error -> new IllegalStateException("Expected success but got failure: " + error));

        var questionnaire = QuestionnaireFactory
                .createNew("survey_001", "MOBILE", "SATISFACTION_JOURNEY",
                        "Customer Satisfaction Survey", AuditTestData.createdAudit())
                .flatMap(builder -> builder
                        .withQuestion(configuredQuestion1)
                        .withQuestion(configuredQuestion2)
                        .build())
                .getOrElseThrow(error -> new IllegalStateException("Expected success but got failure: " + error));

        assertAll(
                () -> assertEquals("survey_001", questionnaire.id()),
                () -> assertEquals("MOBILE", questionnaire.channelDistributionId()),
                () -> assertEquals("SATISFACTION_JOURNEY", questionnaire.journeyDistributionId()),
                () -> assertEquals("Customer Satisfaction Survey", questionnaire.description()),
                () -> assertEquals(ParameterizationStatus.DRAFT, questionnaire.status()),
                () -> assertEquals(2, questionnaire.getOrderedQuestions().size()),
                () -> assertEquals("q_one", questionnaire.getOrderedQuestions().getFirst().id()),
                () -> assertEquals("q_two", questionnaire.getOrderedQuestions().get(1).id()),
                () -> assertEquals("How satisfied are you?", questionnaire.getOrderedQuestions().getFirst().label()),
                () -> assertEquals("Would you recommend us?", questionnaire.getOrderedQuestions().get(1).label())
        );

        // Respostas do Usuário
        Map<String, Object> answers = new HashMap<>();
        answers.put("q_one", "8");
        answers.put("q_two", "yes");

        var result = questionnaire.answerValidation(answers);
        assertTrue(result.isFailure());

        List<QuestionValidationFailure> error = result.errorOrElseThrow(() ->
                new IllegalStateException("Expected failure"));
        assertEquals(2, error.size());
        assertEquals("q_one", error.getFirst().questionId());

        QuestionnaireTree tree = questionnaire.toTree();
        assertEquals(2, tree.questions().size());
        assertEquals("q_one", tree.questions().getFirst().question().id());
        assertEquals("NUMBER", tree.questions().getFirst().answerConfiguration().type().name());
    }

}
