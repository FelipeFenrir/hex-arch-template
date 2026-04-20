package com.acme.orderquestionnaire.domain.questionnaire.vo;

import com.acme.shared.stereotypes.test.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@UnitTest
@DisplayName("QuestionnaireId")
class QuestionnaireIdTest {

    @Test
    @DisplayName("Should create composed identifier from static factory")
    void shouldCreateComposedIdentifier() {
        QuestionnaireId questionnaireId = QuestionnaireId.of("survey", "channel", "journey");

        assertEquals("survey", questionnaireId.id());
        assertEquals("channel", questionnaireId.channelDistributionId());
        assertEquals("journey", questionnaireId.journeyDistributionId());
    }

    @Test
    @DisplayName("Should fail when any identifier component is null")
    void shouldFailWhenAnyComponentIsNull() {
        assertThrows(NullPointerException.class, () -> new QuestionnaireId(null, "channel", "journey"));
        assertThrows(NullPointerException.class, () -> new QuestionnaireId("survey", null, "journey"));
        assertThrows(NullPointerException.class, () -> new QuestionnaireId("survey", "channel", null));
    }
}

