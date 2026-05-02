package com.acme.orderquestionnaire.domain.unit.domain.questionnaire.answer.strategy;

import com.acme.orderquestionnaire.domain.questionnaire.answer.strategy.AnswerConfigurationFactory;
import com.acme.orderquestionnaire.domain.questionnaire.answer.strategy.AnswerDateStrategy;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;
import com.acme.shared.stereotypes.test.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@UnitTest
@DisplayName("AnswerDateStrategy")
class AnswerDateStrategyTest {

    @Test
    @DisplayName("When future date is provided and past dates are blocked then should succeed")
    void shouldValidateFutureDate() {
        AnswerDateStrategy strategy = AnswerConfigurationFactory.createDateStrategy("yyyy-MM-dd",
                false);

        Result<Void, List<DomainError>> validation = strategy.validate(LocalDate.now().plusDays(1));
        assertInstanceOf(Result.Success.class, validation);
    }

    @Test
    @DisplayName("When past date is provided and past dates are blocked then should fail")
    void shouldFailPastDate() {
        AnswerDateStrategy strategy = AnswerConfigurationFactory.createDateStrategy("yyyy-MM-dd",
                false);

        Result<Void, List<DomainError>> validation = strategy.validate(LocalDate.now().minusDays(1));
        assertInstanceOf(Result.Failure.class, validation);
        var failure = (Result.Failure<Void, List<DomainError>>) validation;
        assertEquals("PAST_DATE_NOT_ALLOWED", failure.error().getFirst().code());
    }

    @Test
    @DisplayName("When exporting tree node then should include date attributes")
    void shouldExportTreeNode() {
        AnswerDateStrategy strategy = AnswerConfigurationFactory.createDateStrategy("yyyy-MM-dd",
                true, "custom");

        var node = strategy.toTreeNode();
        assertEquals("DATE", node.type().name());
        assertEquals("yyyy-MM-dd", node.attributes().get("maskFormat"));
    }
}

