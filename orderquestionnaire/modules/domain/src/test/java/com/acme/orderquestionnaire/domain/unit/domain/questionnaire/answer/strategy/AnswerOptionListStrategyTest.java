package com.acme.orderquestionnaire.domain.unit.domain.questionnaire.answer.strategy;

import com.acme.orderquestionnaire.domain.questionnaire.answer.AnswerOptionItem;
import com.acme.orderquestionnaire.domain.questionnaire.answer.strategy.AnswerConfigurationFactory;
import com.acme.orderquestionnaire.domain.questionnaire.answer.strategy.AnswerOptionListStrategy;
import com.acme.orderquestionnaire.domain.questionnaire.tree.AnswerOptionTreeNode;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;
import com.acme.shared.stereotypes.test.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@UnitTest
@DisplayName("AnswerOptionListStrategy")
class AnswerOptionListStrategyTest {

    @Test
    @DisplayName("When answer value matches a listed option then validation should succeed")
    void shouldValidateValidOption() {
        AnswerOptionItem yes = AnswerOptionItem.rehydrate("yes", "Yes");
        AnswerOptionItem no = AnswerOptionItem.rehydrate("no", "No");

        AnswerOptionListStrategy strategy = AnswerConfigurationFactory.createListStrategy(List.of(yes, no));
        Result<Void, List<DomainError>> validation = strategy.validate("yes");

        assertInstanceOf(Result.Success.class, validation);
    }

    @Test
    @DisplayName("When answer value is not listed then validation should fail")
    void shouldFailForUnknownOption() {
        AnswerOptionItem yes = AnswerOptionItem.rehydrate("yes", "Yes");
        AnswerOptionListStrategy strategy = AnswerConfigurationFactory.createListStrategy(List.of(yes));

        Result<Void, List<DomainError>> validation = strategy.validate("unknown");

        assertInstanceOf(Result.Failure.class, validation);
        var failure = (Result.Failure<Void, List<DomainError>>) validation;
        assertEquals("INVALID_OPTION", failure.error().getFirst().code());
    }

    @Test
    @DisplayName("When exporting tree node then should include options")
    void shouldExportTreeNode() {
        AnswerOptionItem yes = AnswerOptionItem.rehydrate("yes", "Yes");
        AnswerOptionListStrategy strategy = AnswerConfigurationFactory.createListStrategy(List.of(yes));

        var node = strategy.toTreeNode();
        assertEquals("OPTION_LIST", node.type().name());
        var options = (List<?>) node.attributes().get("answerOptions");
        assertEquals(1, options.size());
        assertInstanceOf(AnswerOptionTreeNode.class, options.getFirst());
        AnswerOptionTreeNode option = (AnswerOptionTreeNode) options.getFirst();
        assertEquals("yes", option.value());
        assertEquals("Yes", option.label());
        assertEquals(2, AnswerOptionTreeNode.class.getRecordComponents().length);
    }
}

