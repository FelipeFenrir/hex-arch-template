package com.acme.orderquestionnaire.domain.unit.domain.questionnaire.conditioner;

import com.acme.orderquestionnaire.domain.questionnaire.conditioner.EqualCondition;
import com.acme.shared.stereotypes.test.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@UnitTest
@DisplayName("EqualCondition")
class EqualConditionTest {

    @Test
    @DisplayName("When answer equals expected value then condition should be satisfied")
    void shouldSatisfyWhenEqual() {
        EqualCondition condition = new EqualCondition("q_one", "yes");
        assertTrue(condition.isSatisfy(Map.of("q_one", "yes")));
    }

    @Test
    @DisplayName("When answer differs expected value then condition should not be satisfied")
    void shouldNotSatisfyWhenDifferent() {
        EqualCondition condition = new EqualCondition("q_one", "yes");
        assertFalse(condition.isSatisfy(Map.of("q_one", "no")));
    }

    @Test
    @DisplayName("When exporting tree node then type should be equal")
    void shouldExportTreeNode() {
        EqualCondition condition = new EqualCondition("q_one", "yes");
        assertEquals("EQUAL", condition.toTreeNode().type());
    }
}

