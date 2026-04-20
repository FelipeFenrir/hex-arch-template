package com.acme.orderquestionnaire.domain.questionnaire.conditioner;

import com.acme.shared.stereotypes.test.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@UnitTest
@DisplayName("CompositeConditionBuilder")
class CompositeConditionBuilderTest {

    @Test
    @DisplayName("When building with two conditions then builder should return composed condition")
    void shouldBuildCompositeCondition() {
        QuestionCondition condition = CompositeConditionBuilder
                .and(new EqualCondition("q1", "yes"))
                .add(new NumericCondition("q2", 5, ">="))
                .build();

        assertTrue(condition.isSatisfy(Map.of("q1", "yes", "q2", 10)));
    }

    @Test
    @DisplayName("When checking builder metadata then should keep operator and size")
    void shouldExposeBuilderMetadata() {
        CompositeConditionBuilder builder = CompositeConditionBuilder
                .or(new EqualCondition("q1", "yes"))
                .add(new EqualCondition("q2", "no"));

        assertEquals(2, builder.size());
        assertFalse(builder.isAndOperator());
    }
}

