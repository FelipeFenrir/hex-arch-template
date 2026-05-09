package com.acme.orderquestionnaire.domain.unit.domain.questionnaire.vo;

import com.acme.orderquestionnaire.domain.questionnaire.vo.QuestionnaireId;
import com.acme.shared.vo.ChannelDistributionId;
import com.acme.shared.vo.JourneyDistributionId;
import com.acme.shared.vo.QuestionnaireCode;
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
        assertEquals("channel", questionnaireId.getChannelDistributionIdValue());
        assertEquals("journey", questionnaireId.getJourneyDistributionIdValue());
    }

    @Test
    @DisplayName("Should fail when any identifier component is null")
    void shouldFailWhenAnyComponentIsNull() {
        assertThrows(NullPointerException.class,
                () -> new QuestionnaireId(null, ChannelDistributionId.of("channel"), JourneyDistributionId.of("journey")));
        assertThrows(NullPointerException.class,
                () -> new QuestionnaireId(QuestionnaireCode.of("survey"), null, JourneyDistributionId.of("journey")));
        assertThrows(NullPointerException.class,
                () -> new QuestionnaireId(QuestionnaireCode.of("survey"), ChannelDistributionId.of("channel"), null));
    }
}

