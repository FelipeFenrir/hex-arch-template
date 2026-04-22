package com.acme.orderquestionnaire.domain.questionnaire.answer.strategy;

import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;
import com.acme.shared.stereotypes.test.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

@UnitTest
@DisplayName("AnswerTextStrategy")
class AnswerTextStrategyTest {

    @Test
    @DisplayName("When text matches regex then validation should succeed")
    void shouldValidateMatchingText() {
        AnswerTextStrategy strategy = AnswerConfigurationFactory.createTextStrategy("^[A-Z]{3}$");

        Result<Void, List<DomainError>> validation = strategy.validate("ABC");
        assertInstanceOf(Result.Success.class, validation);
    }

    @Test
    @DisplayName("When text does not match regex then validation should fail")
    void shouldFailWhenTextDoesNotMatchRegex() {
        AnswerTextStrategy strategy = AnswerConfigurationFactory.createTextStrategy("^[A-Z]{3}$");

        Result<Void, List<DomainError>> validation = strategy.validate("abc");
        assertInstanceOf(Result.Failure.class, validation);
        var failure = (Result.Failure<Void, List<DomainError>>) validation;
        assertEquals("PATTERN_MISMATCH", failure.error().getFirst().code());
    }

    @Test
    @DisplayName("When exporting tree node then should include regex")
    void shouldExportTreeNode() {
        AnswerTextStrategy strategy = AnswerConfigurationFactory.createTextStrategy("regex", "custom");

        var node = strategy.toTreeNode();
        assertEquals("TEXT", node.type().name());
        assertEquals("regex", node.attributes().get("regexPattern"));
    }
}

