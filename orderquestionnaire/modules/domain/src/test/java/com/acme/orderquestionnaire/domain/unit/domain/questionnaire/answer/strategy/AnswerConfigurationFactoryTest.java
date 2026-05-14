package com.acme.orderquestionnaire.domain.unit.domain.questionnaire.answer.strategy;

import com.acme.orderquestionnaire.domain.questionnaire.answer.AnswerOptionItem;
import com.acme.orderquestionnaire.domain.questionnaire.answer.strategy.AnswerConfigurationFactory;
import com.acme.shared.stereotypes.test.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@UnitTest
@DisplayName("AnswerConfigurationFactory")
class AnswerConfigurationFactoryTest {

    @Test
    @DisplayName("When creating strategies then each type should be correct")
    void shouldCreateStrategies() {
        assertEquals("TEXT", AnswerConfigurationFactory.createTextStrategy().getConfigurationType().name());
        assertEquals("NUMBER", AnswerConfigurationFactory.createNumberStrategy().getConfigurationType().name());
        assertEquals("DATE", AnswerConfigurationFactory.createDateStrategy().getConfigurationType().name());
        assertEquals("OPTION_LIST", AnswerConfigurationFactory.createListStrategy(AnswerOptionItem.createNew(
                "a", "A")).getConfigurationType().name());
    }

    @Test
    @DisplayName("When creating list strategy with custom message then object should be created")
    void shouldCreateListStrategyWithCustomMessage() {
        var strategy = AnswerConfigurationFactory.createListStrategy("custom",
                AnswerOptionItem.createNew("a", "A"));
        assertNotNull(strategy);
    }
}

