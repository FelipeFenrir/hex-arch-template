package com.acme.orderquestionnaire.domain.questionnaire;

import com.acme.orderquestionnaire.domain.questionnaire.answer.strategy.AnswerConfigurationFactory;
import com.acme.orderquestionnaire.domain.questionnaire.conditioner.NumericCondition;
import com.acme.orderquestionnaire.domain.questionnaire.tree.QuestionnaireTree;
import com.acme.orderquestionnaire.domain.question.Question;
import com.acme.orderquestionnaire.testutils.mocks.question.QuestionMock;
import com.acme.orderquestionnaire.testutils.mocks.questionnaire.QuestionnaireMock;
import com.acme.shared.pattern.result.Result;
import com.acme.shared.stereotypes.test.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@UnitTest
@DisplayName("Questionnaire")
class QuestionnaireTest {

    private static final Question QUESTION_1 = QuestionMock.DEFAULT_QUESTION_1
            .getOrElseThrow(error -> new IllegalStateException("Expected success but got failure: " + error));
    private static final Question QUESTION_2 = QuestionMock.DEFAULT_QUESTION_2
            .getOrElseThrow(error -> new IllegalStateException("Expected success but got failure: " + error));

    @Test
    @DisplayName("When validating multiple configured questions then should return sorted contextual failures")
    void shouldReturnSortedFailures() {
        ConfiguredQuestion second = ConfiguredQuestion.createNew(
                QUESTION_2,
                AnswerConfigurationFactory.createTextStrategy(),
                2
        );
        ConfiguredQuestion first = ConfiguredQuestion.createNew(
                QUESTION_1,
                AnswerConfigurationFactory.createNumberStrategy(),
                1
        );

        Questionnaire questionnaire = QuestionnaireMock.active();
        questionnaire.addQuestions(second, first);

        Result<Void, java.util.List<QuestionValidationFailure>> validation = questionnaire.answerValidation(Map.of());
        assertInstanceOf(Result.Failure.class, validation);
        var failure = (Result.Failure<Void, java.util.List<QuestionValidationFailure>>) validation;
        assertEquals(2, failure.error().size());
        assertEquals("how_satisfied_are_you", failure.error().getFirst().questionId());
        assertEquals(1, failure.error().getFirst().order());
    }

    @Test
    @DisplayName("When exporting tree then questions should be ordered")
    void shouldExportOrderedTree() {
        ConfiguredQuestion second = ConfiguredQuestion.createNew(
                QUESTION_2,
                AnswerConfigurationFactory.createTextStrategy(),
                2
        );
        ConfiguredQuestion first = ConfiguredQuestion.createNew(
                QUESTION_1,
                AnswerConfigurationFactory.createNumberStrategy(),
                1
        );
        first.rootCondition(new NumericCondition("x", 1, ">="));

        Questionnaire questionnaire = QuestionnaireMock.draft();
        questionnaire.addQuestions(second, first);

        QuestionnaireTree tree = questionnaire.toTree();
        assertEquals("how_satisfied_are_you", tree.questions().getFirst().question().id());
        assertEquals("you_recommend_us", tree.questions().get(1).question().id());
    }
}

