package com.acme.shared.engine.rule;

import com.acme.shared.stereotypes.test.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@UnitTest
@DisplayName("Specification")
class RuleTest {

    @Test
    @DisplayName("and should only satisfy when both specifications are true")
    void and_shouldOnlySatisfyWhenBothAreTrue() {
        Rule<Integer> positive = value -> value > 0;
        Rule<Integer> even = value -> value % 2 == 0;

        Rule<Integer> positiveAndEven = positive.and(even);

        assertTrue(positiveAndEven.isSatisfiedBy(2));
        assertFalse(positiveAndEven.isSatisfiedBy(3));
        assertFalse(positiveAndEven.isSatisfiedBy(-2));
    }

    @Test
    @DisplayName("or should satisfy when at least one specification is true")
    void or_shouldSatisfyWhenAtLeastOneIsTrue() {
        Rule<Integer> negative = value -> value < 0;
        Rule<Integer> even = value -> value % 2 == 0;

        Rule<Integer> negativeOrEven = negative.or(even);

        assertTrue(negativeOrEven.isSatisfiedBy(-3));
        assertTrue(negativeOrEven.isSatisfiedBy(4));
        assertFalse(negativeOrEven.isSatisfiedBy(3));
    }

    @Test
    @DisplayName("not should invert the original result")
    void not_shouldInvertOriginalResult() {
        Rule<String> blank = value -> value == null || value.isBlank();

        Rule<String> notBlank = blank.not();

        assertTrue(notBlank.isSatisfiedBy("John"));
        assertFalse(notBlank.isSatisfiedBy(" "));
        assertFalse(notBlank.isSatisfiedBy(null));
    }
}

