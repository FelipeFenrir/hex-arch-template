package com.acme.orderquestionnaire.domain.questionnaire.conditioner;

import com.acme.shared.stereotypes.test.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@UnitTest
@DisplayName("VisibilityCondition")
class VisibilityConditionTest {

    @Test
    @DisplayName("When answer equals expected value then should satisfy")
    void shouldSatisfy() {
        VisibilityCondition condition = new VisibilityCondition("q1", "yes");
        assertTrue(condition.isSatisfy(Map.of("q1", "yes")));
        assertFalse(condition.isSatisfy(Map.of("q1", "no")));
    }

    @Test
    @DisplayName("When exporting tree node then type should be visibility")
    void shouldExportTreeNode() {
        VisibilityCondition condition = new VisibilityCondition("q1", "yes");
        assertEquals("VISIBILITY", condition.toTreeNode().type());
    }
}

