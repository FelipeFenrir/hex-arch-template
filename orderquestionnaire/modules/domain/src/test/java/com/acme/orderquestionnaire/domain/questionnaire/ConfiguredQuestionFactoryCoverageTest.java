package com.acme.orderquestionnaire.domain.questionnaire;

import com.acme.orderquestionnaire.domain.questionnaire.errors.QuestionnaireDomainErrors;
import com.acme.orderquestionnaire.domain.question.Question;
import com.acme.orderquestionnaire.domain.question.answer.AnswerConfiguration;
import com.acme.orderquestionnaire.domain.question.answer.AnswerOptionItem;
import com.acme.orderquestionnaire.domain.questionnaire.conditioner.EqualCondition;
import com.acme.orderquestionnaire.testutils.mocks.question.QuestionMock;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;
import com.acme.shared.stereotypes.test.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@UnitTest
@DisplayName("ConfiguredQuestionFactory additional coverage")
class ConfiguredQuestionFactoryCoverageTest {

    public static final Question QUESTION_1 = QuestionMock.DEFAULT_QUESTION_1
            .getOrElseThrow(error -> new IllegalStateException("Expected success but got failure: " + error));
    public static final Question QUESTION_2 = QuestionMock.DEFAULT_QUESTION_2
            .getOrElseThrow(error -> new IllegalStateException("Expected success but got failure: " + error));

    @Test
    @DisplayName("ConfiguredQuestionFactory should reject null question")
    void shouldRejectNullQuestion() {
        Result<ConfiguredQuestionFactory.QuestionBuilder, List<DomainError>> result = ConfiguredQuestionFactory.from(null);

        assertTrue(result.isFailure());
        List<DomainError> error = result.errorOrElseThrow(() -> new IllegalStateException("Expected failure"));
        assertEquals(List.of(QuestionnaireDomainErrors.requiredObject("question")), error);
    }

    @Test
    @DisplayName("QuestionBuilder should handle null order, condition and all answer types")
    void shouldBuildAllAnswerTypesWithOptionalOrderAndCondition() {

        ConfiguredQuestion textQuestion = ConfiguredQuestionFactory.from(QUESTION_1)
                .flatMap(builder -> builder
                        .withOrder(null)
                        .asText(new ConfiguredQuestionFactory.TextConfig("[0-9]+", "Digits only")))
                .getOrElseThrow(error -> new IllegalStateException("Expected success but got failure: " + error));
        assertEquals(0, textQuestion.order());
        assertNull(textQuestion.rootCondition());

        ConfiguredQuestion numberQuestion = ConfiguredQuestionFactory.from(QUESTION_1)
                .flatMap(builder -> builder
                        .withOrder(2)
                        .withCondition(new EqualCondition("gate", true))
                        .asNumber(new ConfiguredQuestionFactory.NumberConfig(0.0, 10.0, 2.0,
                                false, false, "Number error")))
                .getOrElseThrow(error -> new IllegalStateException("Expected success but got failure: " + error));
        assertEquals(2, numberQuestion.order());
        assertNotNull(numberQuestion.rootCondition());

        ConfiguredQuestion dateQuestion = ConfiguredQuestionFactory.from(QUESTION_1)
                .flatMap(builder -> builder.asDate(new ConfiguredQuestionFactory.DateConfig("yyyy-MM-dd", false, "Future only")))
                .getOrElseThrow(error -> new IllegalStateException("Expected success but got failure: " + error));
        assertEquals("DATE", dateQuestion.answerConfiguration().getConfigurationType().name());

        List<AnswerOptionItem> options = List.of(
                AnswerOptionItem.rehydrate("yes", "Yes"),
                AnswerOptionItem.rehydrate("no", "No")
        );
        ConfiguredQuestion optionQuestion = ConfiguredQuestionFactory.from(QUESTION_1)
                .flatMap(builder -> builder.asOptionList(new ConfiguredQuestionFactory.ListConfig(options, "Select one")))
                .getOrElseThrow(error -> new IllegalStateException("Expected success but got failure: " + error));
        assertEquals("OPTION_LIST", optionQuestion.answerConfiguration().getConfigurationType().name());

        assertInstanceOf(Result.Success.class, textQuestion.validate(Map.of(QUESTION_1.id(), "123")));
        assertInstanceOf(Result.Success.class, numberQuestion.validate(Map.of(QUESTION_1.id(), 4, "gate", true)));
        assertInstanceOf(Result.Success.class, dateQuestion.validate(Map.of(QUESTION_1.id(), LocalDate.now().plusDays(1))));
        assertInstanceOf(Result.Success.class, optionQuestion.validate(Map.of(QUESTION_1.id(), "yes")));

        assertInstanceOf(Result.Success.class,
                ConfiguredQuestionFactory.from(QUESTION_1)
                        .getOrElseThrow(error -> new IllegalStateException("Expected success builder but got failure: " + error))
                        .asNumber(null));
        assertInstanceOf(Result.Success.class,
                ConfiguredQuestionFactory.from(QUESTION_1)
                        .getOrElseThrow(error -> new IllegalStateException("Expected success builder but got failure: " + error))
                        .asDate(null));
        assertInstanceOf(Result.Success.class,
                ConfiguredQuestionFactory.from(QUESTION_1)
                        .getOrElseThrow(error -> new IllegalStateException("Expected success builder but got failure: " + error))
                        .asOptionList((ConfiguredQuestionFactory.ListConfig) null));
    }

    @Test
    @DisplayName("QuestionBuilder should reject negative order and null answer configuration")
    void shouldRejectNegativeOrderAndNullAnswerConfiguration() throws Exception {
        ConfiguredQuestionFactory.QuestionBuilder invalidOrderBuilder = ConfiguredQuestionFactory.from(QUESTION_1)
                .getOrElseThrow(error -> new IllegalStateException("Expected success builder but got failure: " + error));
        Result<ConfiguredQuestion, List<DomainError>> invalidOrder = invalidOrderBuilder.withOrder(-1).asText();
        assertTrue(invalidOrder.isFailure());
        assertEquals(List.of(QuestionnaireDomainErrors.invalidOrder()),
                invalidOrder.errorOrElseThrow(() -> new IllegalStateException("Expected failure")));

        ConfiguredQuestionFactory.QuestionBuilder nullStrategyBuilder = ConfiguredQuestionFactory.from(QUESTION_2)
                .getOrElseThrow(error -> new IllegalStateException("Expected success builder but got failure: " + error));
        Method buildMethod = ConfiguredQuestionFactory.QuestionBuilder.class
                .getDeclaredMethod("build", AnswerConfiguration.class);
        buildMethod.setAccessible(true);

        @SuppressWarnings("unchecked")
        Result<ConfiguredQuestion, List<DomainError>> nullStrategyResult =
                (Result<ConfiguredQuestion, List<DomainError>>) buildMethod.invoke(nullStrategyBuilder, new Object[]{null});

        assertTrue(nullStrategyResult.isFailure());
        assertEquals(List.of(QuestionnaireDomainErrors.requiredObject("answerConfiguration")),
                nullStrategyResult.errorOrElseThrow(() -> new IllegalStateException("Expected failure")));
    }

    @Test
    @DisplayName("ConfiguredQuestion should expose accessors, setter-like methods and validation branches")
    void shouldCoverConfiguredQuestionBehavior() {
        ConfiguredQuestion configuredQuestion = ConfiguredQuestionFactory.from(QUESTION_1)
                .flatMap(builder -> builder.withOrder(3).asText())
                .getOrElseThrow(error -> new IllegalStateException("Expected success but got failure: " + error));

        configuredQuestion.order(5);
        configuredQuestion.rootCondition(new EqualCondition("show", true));

        assertSame(QUESTION_1, configuredQuestion.question());
        assertEquals(5, configuredQuestion.order());
        assertNotNull(configuredQuestion.answerConfiguration());
        assertNotNull(configuredQuestion.rootCondition());

        assertInstanceOf(Result.Success.class, configuredQuestion.validate(Map.of("show", false)));
        assertInstanceOf(Result.Success.class, configuredQuestion.validate(Map.of(
                QUESTION_1.id(), "valid",
                "show", true
        )));

        Result<Void, QuestionValidationFailure> failure = configuredQuestion.validate(Map.of("show", true));
        assertTrue(failure.isFailure());
        assertEquals("how_satisfied_are_you", failure.errorOrElseThrow(() ->
                new IllegalStateException("Expected failure")).questionId());
        assertNotNull(configuredQuestion.toTreeNode());
    }
}


