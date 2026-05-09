package com.acme.orderquestionnaire.domain.unit.domain.questionnaire;

import com.acme.orderquestionnaire.domain.question.Question;
import com.acme.orderquestionnaire.domain.questionnaire.ConfiguredQuestion;
import com.acme.orderquestionnaire.domain.questionnaire.ConfiguredQuestionFactory;
import com.acme.orderquestionnaire.domain.questionnaire.QuestionValidationFailure;
import com.acme.orderquestionnaire.domain.questionnaire.answer.AnswerOptionComposer;
import com.acme.orderquestionnaire.domain.questionnaire.answer.AnswerOptionItem;
import com.acme.orderquestionnaire.domain.questionnaire.conditioner.QuestionConditionComposer;
import com.acme.orderquestionnaire.domain.unit.testutils.mocks.question.QuestionMock;
import com.acme.shared.pattern.result.Result;
import com.acme.shared.stereotypes.test.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@UnitTest
@DisplayName("ConfiguredQuestionFactory")
class ConfiguredQuestionFactoryTest {

    public static final Question QUESTION_1 = QuestionMock.DEFAULT_QUESTION_1.getOrElseThrow(error -> new IllegalStateException("Expected success but got failure: " + error));
    public static final Question QUESTION_2 = QuestionMock.DEFAULT_QUESTION_2.getOrElseThrow(error -> new IllegalStateException("Expected success but got failure: " + error));

    @Test
    @DisplayName("When creating a text question without condition, it should build successfully")
    void shouldCreateTextQuestionWithoutCondition() {

        ConfiguredQuestion configuredQuestion = ConfiguredQuestionFactory.from(QUESTION_1)
                .flatMap(ConfiguredQuestionFactory.ConfiguredQuestionBuilder::asText)
                .getOrElseThrow(error -> new IllegalStateException("Expected success but got failure: " + error));

        assertNotNull(configuredQuestion);
        assertEquals(QUESTION_1, configuredQuestion.question());
        assertNull(configuredQuestion.rootCondition());
    }

    @Test
    @DisplayName("When creating a number question with condition, it should apply the condition")
    void shouldCreateNumberQuestionWithCondition() {
        var condition = QuestionConditionComposer.condition(
                new com.acme.orderquestionnaire.domain.questionnaire.conditioner.NumericCondition("q_one",
                        5, ">")
        ).build();

        ConfiguredQuestion configuredQuestion = ConfiguredQuestionFactory.from(QUESTION_1)
                .flatMap(builder -> builder
                        .withCondition(condition)
                        .asNumber())
                .getOrElseThrow(error -> new IllegalStateException("Expected success but got failure: " + error));

        assertNotNull(configuredQuestion.rootCondition());
        assertSame(condition, configuredQuestion.rootCondition());
    }

    @Test
    @DisplayName("When creating a date question with order, it should set the order")
    void shouldCreateDateQuestionWithOrder() {

        ConfiguredQuestion configuredQuestion = ConfiguredQuestionFactory.from(QUESTION_1)
                .flatMap(builder -> builder
                        .withOrder(5)
                        .asDate())
                .getOrElseThrow(error -> new IllegalStateException("Expected success but got failure: " + error));

        assertEquals(5, configuredQuestion.order());
    }

    @Test
    @DisplayName("When creating option list question with options, it should include them")
    void shouldCreateOptionListQuestionWithOptions() {
        List<AnswerOptionItem> options = List.of(
                AnswerOptionItem.createNew("yes", "Yes"),
                AnswerOptionItem.createNew("no", "No")
        );

        ConfiguredQuestion configuredQuestion = ConfiguredQuestionFactory.from(QUESTION_2)
                .flatMap(builder -> builder.asOptionList(new ConfiguredQuestionFactory.ListConfig(options, null)))
                .getOrElseThrow(error -> new IllegalStateException("Expected success but got failure: " + error));

        assertNotNull(configuredQuestion);
        assertEquals(QUESTION_2, configuredQuestion.question());
    }

    @Test
    @DisplayName("When question is visible without condition, isVisible should return true")
    void shouldReturnTrueWhenNoCondition() {
        ConfiguredQuestion configuredQuestion = ConfiguredQuestionFactory.from(QUESTION_1)
                .flatMap(ConfiguredQuestionFactory.ConfiguredQuestionBuilder::asText)
                .getOrElseThrow(error -> new IllegalStateException("Expected success but got failure: " + error));

        Map<String, Object> answers = new HashMap<>();
        assertTrue(configuredQuestion.isVisible(answers));
    }

    @Test
    @DisplayName("When question has condition and is satisfied, isVisible should return true")
    void shouldReturnTrueWhenConditionSatisfied() {
        var condition = QuestionConditionComposer.condition(
                new com.acme.orderquestionnaire.domain.questionnaire.conditioner.NumericCondition("q_one",
                        5, ">")
        ).build();

        ConfiguredQuestion configuredQuestion = ConfiguredQuestionFactory.from(QUESTION_1)
                .flatMap(builder -> builder
                        .withCondition(condition)
                        .asText())
                .getOrElseThrow(error -> new IllegalStateException("Expected success but got failure: " + error));

        Map<String, Object> answers = new HashMap<>();
        answers.put("q_one", 6);
        assertTrue(configuredQuestion.isVisible(answers));
    }

    @Test
    @DisplayName("When question with order and condition, should build with all properties")
    void shouldCreateQuestionWithAllProperties() {
        var condition = QuestionConditionComposer.condition(
                new com.acme.orderquestionnaire.domain.questionnaire.conditioner.NumericCondition(
                        "satisfaction", 5, ">")
        ).build();

        ConfiguredQuestion configuredQuestion = ConfiguredQuestionFactory.from(QUESTION_2)
                .flatMap(builder -> builder
                        .withOrder(2)
                        .withCondition(condition)
                        .asOptionList(AnswerOptionComposer.options(
                                AnswerOptionComposer.option("yes", "Yes"),
                                AnswerOptionComposer.option("no", "No")
                        )))
                .getOrElseThrow(error -> new IllegalStateException("Expected success but got failure: " + error));

        assertEquals(2, configuredQuestion.order());
        assertNotNull(configuredQuestion.rootCondition());
        assertEquals(QUESTION_2, configuredQuestion.question());
    }

    @Test
    @DisplayName("When validating visible question with missing answer, should return failure")
    void shouldReturnFailureForMissingMandatoryAnswer() {
        ConfiguredQuestion configuredQuestion = ConfiguredQuestionFactory.from(QUESTION_1)
                .flatMap(ConfiguredQuestionFactory.ConfiguredQuestionBuilder::asText)
                .getOrElseThrow(error -> new IllegalStateException("Expected success but got failure: " + error));

        Result<Void, QuestionValidationFailure> result = configuredQuestion.validate(new HashMap<>());

        assertInstanceOf(Result.Failure.class, result);
        QuestionValidationFailure failure = result.errorOrElseThrow(() ->
                new IllegalStateException("Expected failure"));
        assertEquals(QUESTION_1.id(), failure.questionId());
        assertEquals(1, failure.errors().size());
        assertEquals("MANDATORY_ANSWER", failure.errors().getFirst().code());
    }

}
