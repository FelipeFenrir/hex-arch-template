package com.acme.orderquestionnaire.domain.questionnaire.conditioner;

import com.acme.shared.stereotypes.test.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@UnitTest
@DisplayName("NumericCondition")
class NumericConditionTest {

    @Test
    @DisplayName("When operator is greater than and answer is higher then should satisfy")
    void shouldSatisfyGreaterThan() {
        NumericCondition condition = new NumericCondition("q1", 5, ">");
        assertTrue(condition.isSatisfy(Map.of("q1", 6)));
        assertFalse(condition.isSatisfy(Map.of("q1", 3)));
    }

    @Test
    @DisplayName("When operator is invalid then should throw")
    void shouldThrowForInvalidOperator() {
        assertThrows(IllegalArgumentException.class, () -> new NumericCondition("q1", 5, "<>") );
    }

    @Test
    @DisplayName("When exporting tree node then type should be numeric")
    void shouldExportTreeNode() {
        NumericCondition condition = new NumericCondition("q1", 5, "==");
        assertEquals("NUMERIC", condition.toTreeNode().type());
    }
}

