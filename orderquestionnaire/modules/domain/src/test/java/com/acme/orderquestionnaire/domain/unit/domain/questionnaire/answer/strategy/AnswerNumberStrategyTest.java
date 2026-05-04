package com.acme.orderquestionnaire.domain.unit.domain.questionnaire.answer.strategy;

import com.acme.orderquestionnaire.domain.questionnaire.answer.strategy.AnswerConfigurationFactory;
import com.acme.orderquestionnaire.domain.questionnaire.answer.strategy.AnswerNumberStrategy;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;
import com.acme.shared.stereotypes.test.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@UnitTest
@DisplayName("AnswerNumberStrategy")
class AnswerNumberStrategyTest {

    @Test
    @DisplayName("When number respects constraints then validation should succeed")
    void shouldValidateNumber() {
        AnswerNumberStrategy strategy = AnswerConfigurationFactory.createNumberStrategy(0.0, 10.0, 1.0,
                false, false, null);

        Result<Void, List<DomainError>> validation = strategy.validate(8);
        assertInstanceOf(Result.Success.class, validation);
    }

    @Test
    @DisplayName("When number breaks constraints then should accumulate errors")
    void shouldAccumulateErrors() {
        AnswerNumberStrategy strategy = AnswerConfigurationFactory.createNumberStrategy(0.0, 10.0, 2.0,
                false, false, null);

        Result<Void, List<DomainError>> validation = strategy.validate(-1.5);
        assertInstanceOf(Result.Failure.class, validation);
        var failure = (Result.Failure<Void, List<DomainError>>) validation;
        assertEquals(4, failure.error().size());
    }

    @Test
    @DisplayName("When exporting tree node then should include min and max")
    void shouldExportTreeNode() {
        AnswerNumberStrategy strategy = AnswerConfigurationFactory.createNumberStrategy(1.0, 9.0, null,
                true, true, null);

        var node = strategy.toTreeNode();
        assertEquals("NUMBER", node.type().name());
        assertEquals(1.0, node.attributes().get("min"));
        assertEquals(9.0, node.attributes().get("max"));
    }
}

