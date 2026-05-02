package com.acme.orderquestionnaire.domain.unit.domain.questionnaire.conditioner;

import com.acme.orderquestionnaire.domain.questionnaire.conditioner.CompositeCondition;
import com.acme.orderquestionnaire.domain.questionnaire.conditioner.CompositeConditionBuilder;
import com.acme.orderquestionnaire.domain.questionnaire.conditioner.EqualCondition;
import com.acme.orderquestionnaire.domain.questionnaire.conditioner.NumericCondition;
import com.acme.orderquestionnaire.domain.questionnaire.conditioner.QuestionCondition;
import com.acme.orderquestionnaire.domain.questionnaire.conditioner.VisibilityCondition;
import com.acme.shared.stereotypes.test.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@UnitTest
@DisplayName("Conditioners additional coverage")
class ConditionersCoverageTest {

    @Test
    @DisplayName("NumericCondition should evaluate every operator and invalid inputs")
    void shouldEvaluateNumericConditionAcrossAllOperators() {
        Map<String, Object> answers = Map.of("score", 10, "other", "x");

        assertTrue(new NumericCondition("score", 5, NumericCondition.ComparisonOperator.GREATER_THAN).isSatisfy(answers));
        assertTrue(new NumericCondition("score", 10, ">=").isSatisfy(answers));
        assertTrue(new NumericCondition("score", 12, "<").isSatisfy(answers));
        assertTrue(new NumericCondition("score", 10, "<=").isSatisfy(answers));
        assertTrue(new NumericCondition("score", 10, "==").isSatisfy(answers));
        assertTrue(new NumericCondition("score", 5, "!=").isSatisfy(answers));
        assertFalse(new NumericCondition("score", 10, "<").isSatisfy(answers));
        assertFalse(new NumericCondition("score", 10, "!=").isSatisfy(answers));
        assertFalse(new NumericCondition("other", 1, ">=").isSatisfy(answers));

        assertSame(NumericCondition.ComparisonOperator.GREATER_THAN,
                NumericCondition.ComparisonOperator.fromString(">"));
        assertSame(NumericCondition.ComparisonOperator.GREATER_OR_EQUAL,
                NumericCondition.ComparisonOperator.fromString(">="));
        assertSame(NumericCondition.ComparisonOperator.LESS_THAN,
                NumericCondition.ComparisonOperator.fromString("<"));
        assertSame(NumericCondition.ComparisonOperator.LESS_OR_EQUAL,
                NumericCondition.ComparisonOperator.fromString("<="));
        assertSame(NumericCondition.ComparisonOperator.EQUAL,
                NumericCondition.ComparisonOperator.fromString("=="));
        assertSame(NumericCondition.ComparisonOperator.DIFFERENT,
                NumericCondition.ComparisonOperator.fromString("!="));
        assertThrows(IllegalArgumentException.class,
                () -> NumericCondition.ComparisonOperator.fromString("<>"));

        assertEquals("NUMERIC", new NumericCondition("score", 5, ">=").toTreeNode().type());
    }

    @Test
    @DisplayName("CompositeConditionBuilder should support empty, single and multiple conditions")
    void shouldBuildCompositeConditionsInAllShapes() throws Exception {
        QuestionCondition first = new EqualCondition("answer", "yes");
        QuestionCondition second = new VisibilityCondition("visible", true);

        CompositeConditionBuilder andBuilder = CompositeConditionBuilder.and(first);
        assertTrue(andBuilder.isAndOperator());
        assertEquals(1, andBuilder.size());
        assertSame(first, andBuilder.build());

        CompositeConditionBuilder orBuilder = CompositeConditionBuilder.or(first).add(second);
        assertFalse(orBuilder.isAndOperator());
        assertEquals(2, orBuilder.size());
        QuestionCondition composite = orBuilder.build();
        assertInstanceOf(CompositeCondition.class, composite);
        assertTrue(composite.isSatisfy(Map.of("answer", "no", "visible", true)));
        assertEquals("OR", composite.toTreeNode().attributes().get("operator"));

        CompositeConditionBuilder emptyBuilder = CompositeConditionBuilder.and(first);
        Field conditionsField = CompositeConditionBuilder.class.getDeclaredField("conditions");
        conditionsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<QuestionCondition> conditions = (List<QuestionCondition>) conditionsField.get(emptyBuilder);
        conditions.clear();

        QuestionCondition alwaysSatisfied = emptyBuilder.build();
        assertInstanceOf(CompositeCondition.class, alwaysSatisfied);
        assertTrue(alwaysSatisfied.isSatisfy(Map.of()));
        assertNotNull(alwaysSatisfied.toTreeNode());
    }
}


