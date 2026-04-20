package com.acme.orderquestionnaire.domain;

import com.acme.orderquestionnaire.domain.question.enumerator.AnswerType;
import com.acme.orderquestionnaire.domain.question.errors.QuestionDomainErrors;
import com.acme.orderquestionnaire.domain.questionnaire.errors.QuestionnaireDomainErrors;
import com.acme.orderquestionnaire.domain.questionnaire.QuestionValidationFailure;
import com.acme.orderquestionnaire.domain.questionnaire.tree.AnswerConfigurationTreeNode;
import com.acme.orderquestionnaire.domain.questionnaire.tree.QuestionConditionTreeNode;
import com.acme.orderquestionnaire.domain.questionnaire.tree.QuestionnaireTree;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.stereotypes.test.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@UnitTest
@DisplayName("Common domain coverage")
class CommonDomainCoverageTest {

    @Test
    @DisplayName("QuestionDomainErrors should expose all mapped codes and messages")
    void shouldCreateAllQuestionDomainErrors() {
        assertEquals(new DomainError("MANDATORY_ANSWER", "Mandatory: Label"),
                QuestionDomainErrors.mandatoryAnswer("Label"));
        assertEquals(new DomainError("INVALID_ANSWER_TYPE", "Answer must be a string."),
                QuestionDomainErrors.invalidAnswerType("string"));
        assertEquals(new DomainError("PATTERN_MISMATCH", "custom"),
                QuestionDomainErrors.patternMismatch("custom"));
        assertEquals(new DomainError("DECIMAL_NOT_ALLOWED", "Decimal numbers not allowed."),
                QuestionDomainErrors.decimalNotAllowed());
        assertEquals(new DomainError("NEGATIVE_NOT_ALLOWED", "Negative numbers not allowed."),
                QuestionDomainErrors.negativeNotAllowed());
        assertEquals(new DomainError("VALUE_BELOW_MIN", "Value below minimum."),
                QuestionDomainErrors.valueBelowMin());
        assertEquals(new DomainError("VALUE_ABOVE_MAX", "Value above maximum."),
                QuestionDomainErrors.valueAboveMax());
        assertEquals(new DomainError("INVALID_STEP", "The value must respect the step of: 2.5"),
                QuestionDomainErrors.invalidStep(2.5));
        assertEquals(new DomainError("PAST_DATE_NOT_ALLOWED", "Past dates are not allowed."),
                QuestionDomainErrors.pastDateNotAllowed());
        assertEquals(new DomainError("INVALID_OPTION", "Invalid or inactive option."),
                QuestionDomainErrors.invalidOption());
        assertEquals(new DomainError("REQUIRED_FIELD", "field must not be blank"),
                QuestionDomainErrors.requiredField("field"));
        assertEquals(new DomainError("REQUIRED_OBJECT", "object must not be null"),
                QuestionDomainErrors.requiredObject("object"));
    }

    @Test
    @DisplayName("QuestionnaireDomainErrors should expose all mapped codes and messages")
    void shouldCreateAllQuestionnaireDomainErrors() {
        assertEquals(new DomainError("MANDATORY_ANSWER", "Mandatory: Label"),
                QuestionnaireDomainErrors.mandatoryAnswer("Label"));
        assertEquals(new DomainError("REQUIRED_FIELD", "field must not be blank"),
                QuestionnaireDomainErrors.requiredField("field"));
        assertEquals(new DomainError("REQUIRED_OBJECT", "object must not be null"),
                QuestionnaireDomainErrors.requiredObject("object"));
        assertEquals(new DomainError("INVALID_ORDER", "order must be greater than or equal to zero"),
                QuestionnaireDomainErrors.invalidOrder());
    }

    @Test
    @DisplayName("Tree records and failures should defensively copy mutable inputs")
    void shouldDefensivelyCopyTreeNodesAndFailures() {
        Map<String, Object> attributes = new LinkedHashMap<>();
        attributes.put("regexPattern", "[A-Z]+");
        AnswerConfigurationTreeNode answerNode = new AnswerConfigurationTreeNode(AnswerType.TEXT, attributes);
        AnswerConfigurationTreeNode emptyAnswerNode = new AnswerConfigurationTreeNode(AnswerType.OBJECT, null);
        attributes.put("newKey", "newValue");

        assertEquals(1, answerNode.attributes().size());
        assertEquals("[A-Z]+", answerNode.attributes().get("regexPattern"));
        assertTrue(emptyAnswerNode.attributes().isEmpty());
        assertThrows(UnsupportedOperationException.class, () -> answerNode.attributes().put("x", "y"));

        QuestionConditionTreeNode child = new QuestionConditionTreeNode("EQUAL", null, null);
        Map<String, Object> conditionAttributes = new LinkedHashMap<>();
        conditionAttributes.put("operator", "AND");
        List<QuestionConditionTreeNode> children = new ArrayList<>();
        children.add(child);
        QuestionConditionTreeNode conditionNode = new QuestionConditionTreeNode("COMPOSITE", conditionAttributes, children);
        conditionAttributes.put("ignored", true);
        children.clear();

        assertEquals("COMPOSITE", conditionNode.type());
        assertEquals("AND", conditionNode.attributes().get("operator"));
        assertEquals(1, conditionNode.children().size());
        assertTrue(conditionNode.children().contains(child));
        assertThrows(UnsupportedOperationException.class, () -> conditionNode.attributes().put("x", "y"));
        assertThrows(UnsupportedOperationException.class, () -> conditionNode.children().add(child));

        List<DomainError> errors = new ArrayList<>();
        errors.add(QuestionnaireDomainErrors.mandatoryAnswer("Question"));
        QuestionValidationFailure failure = new QuestionValidationFailure("q1", "Question", 1, errors);
        QuestionValidationFailure failureWithNullErrors = new QuestionValidationFailure("q2", "Question 2", 2, null);
        errors.clear();

        assertEquals("q1", failure.questionId());
        assertEquals("Question", failure.questionLabel());
        assertEquals(1, failure.order());
        assertEquals(1, failure.errors().size());
        assertTrue(failureWithNullErrors.errors().isEmpty());
        assertThrows(UnsupportedOperationException.class, () -> failure.errors().add(new DomainError("X", "Y")));

        QuestionnaireTree questionnaireTree = new QuestionnaireTree("survey", "channel", "journey",
                "description", "DRAFT", null);
        assertTrue(questionnaireTree.questions().isEmpty());
    }
}



