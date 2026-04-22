package com.acme.orderquestionnaire.domain.questionnaire.answer;

import com.acme.shared.stereotypes.test.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@UnitTest
@DisplayName("AnswerOptionItem")
class AnswerOptionItemTest {

    @Test
    @DisplayName("When creating new option then value and label should be set")
    void shouldCreateOption() {
        AnswerOptionItem option = AnswerOptionItem.createNew("yes", "Yes");

        assertEquals("yes", option.value());
        assertEquals("Yes", option.label());
    }

    @Test
    @DisplayName("When rehydrating option then value and label should be preserved")
    void shouldRehydrateOption() {
        AnswerOptionItem option = AnswerOptionItem.rehydrate("no", "No");

        assertEquals("no", option.value());
        assertEquals("No", option.label());
    }
}

