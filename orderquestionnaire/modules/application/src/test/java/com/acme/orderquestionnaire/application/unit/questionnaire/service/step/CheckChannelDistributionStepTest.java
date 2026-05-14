package com.acme.orderquestionnaire.application.unit.questionnaire.service.step;

import com.acme.orderquestionnaire.application.channel.port.out.ChannelDistributionOutPort;
import com.acme.orderquestionnaire.application.questionnaire.dto.command.CreateQuestionnaireCommand;
import com.acme.orderquestionnaire.application.questionnaire.service.context.CreateQuestionnairePipelineContext;
import com.acme.orderquestionnaire.application.questionnaire.service.step.CheckChannelDistributionStep;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;
import com.acme.shared.stereotypes.test.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@UnitTest
@DisplayName("CheckChannelDistributionStep")
class CheckChannelDistributionStepTest {

    @Test
    @DisplayName("should succeed when channel distribution exists")
    void shouldSucceedWhenChannelDistributionExists() {
        ChannelDistributionOutPort outPort = mock(ChannelDistributionOutPort.class);
        when(outPort.existsById("APP")).thenReturn(true);

        CheckChannelDistributionStep step = new CheckChannelDistributionStep(outPort);
        CreateQuestionnairePipelineContext context = contextWithChannelId("APP");

        Result<Void, List<DomainError>> result = step.execute(context);

        assertInstanceOf(Result.Success.class, result);
    }

    @Test
    @DisplayName("should fail with CHANNEL_DISTRIBUTION_NOT_FOUND when channel does not exist")
    void shouldFailWhenChannelDistributionDoesNotExist() {
        ChannelDistributionOutPort outPort = mock(ChannelDistributionOutPort.class);
        when(outPort.existsById("UNKNOWN")).thenReturn(false);

        CheckChannelDistributionStep step = new CheckChannelDistributionStep(outPort);
        CreateQuestionnairePipelineContext context = contextWithChannelId("UNKNOWN");

        Result<Void, List<DomainError>> result = step.execute(context);

        assertInstanceOf(Result.Failure.class, result);
        List<DomainError> errors = result.errorOrElseThrow(() -> new IllegalStateException("Expected failure"));
        assertTrue(errors.stream().anyMatch(e -> e.code().equals("CHANNEL_DISTRIBUTION_NOT_FOUND")));
    }

    private static CreateQuestionnairePipelineContext contextWithChannelId(String channelId) {
        var cmd = new CreateQuestionnaireCommand("q_001", channelId, "JOURNEY_01", "Desc", null, null);
        return new CreateQuestionnairePipelineContext(cmd);
    }
}

