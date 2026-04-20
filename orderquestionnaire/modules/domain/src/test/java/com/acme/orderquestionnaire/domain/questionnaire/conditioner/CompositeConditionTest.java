package com.acme.orderquestionnaire.domain.questionnaire.conditioner;

import com.acme.shared.stereotypes.test.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@UnitTest
@DisplayName("CompositeCondition")
class CompositeConditionTest {

    @Test
    @DisplayName("When operator is AND then all conditions should be true")
    void shouldApplyAnd() {
        CompositeCondition condition = new CompositeCondition(true);
        condition.addCondition(new EqualCondition("q1", "yes"));
        condition.addCondition(new NumericCondition("q2", 5, ">="));

        assertTrue(condition.isSatisfy(Map.of("q1", "yes", "q2", 9)));
        assertFalse(condition.isSatisfy(Map.of("q1", "yes", "q2", 1)));
    }

    @Test
    @DisplayName("When operator is OR then any condition true should satisfy")
    void shouldApplyOr() {
        CompositeCondition condition = new CompositeCondition(false);
        condition.addCondition(new EqualCondition("q1", "yes"));
        condition.addCondition(new NumericCondition("q2", 5, ">="));

        assertTrue(condition.isSatisfy(Map.of("q1", "no", "q2", 9)));
        assertFalse(condition.isSatisfy(Map.of("q1", "no", "q2", 1)));
    }

    @Test
    @DisplayName("When exporting tree node then should contain children")
    void shouldExportTreeNode() {
        CompositeCondition condition = new CompositeCondition(true);
        condition.addCondition(new EqualCondition("q1", "yes"));
        condition.addCondition(new NumericCondition("q2", 5, ">="));

        var node = condition.toTreeNode();
        assertEquals("COMPOSITE", node.type());
        assertEquals(2, node.children().size());
    }
}

