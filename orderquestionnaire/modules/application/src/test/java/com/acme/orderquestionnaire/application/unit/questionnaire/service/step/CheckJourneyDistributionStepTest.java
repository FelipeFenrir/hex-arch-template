package com.acme.orderquestionnaire.application.unit.questionnaire.service.step;

import com.acme.orderquestionnaire.application.journey.port.out.JourneyDistributionOutPort;
import com.acme.orderquestionnaire.application.questionnaire.dto.command.CreateQuestionnaireCommand;
import com.acme.orderquestionnaire.application.questionnaire.service.context.CreateQuestionnairePipelineContext;
import com.acme.orderquestionnaire.application.questionnaire.service.step.CheckJourneyDistributionStep;
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
@DisplayName("CheckJourneyDistributionStep")
class CheckJourneyDistributionStepTest {

    @Test
    @DisplayName("should succeed when journey distribution exists")
    void shouldSucceedWhenJourneyDistributionExists() {
        JourneyDistributionOutPort outPort = mock(JourneyDistributionOutPort.class);
        when(outPort.existsById("JOURNEY_01")).thenReturn(true);

        CheckJourneyDistributionStep step = new CheckJourneyDistributionStep(outPort);
        CreateQuestionnairePipelineContext context = contextWithJourneyId("JOURNEY_01");

        Result<Void, List<DomainError>> result = step.execute(context);

        assertInstanceOf(Result.Success.class, result);
    }

    @Test
    @DisplayName("should fail with JOURNEY_DISTRIBUTION_NOT_FOUND when journey does not exist")
    void shouldFailWhenJourneyDistributionDoesNotExist() {
        JourneyDistributionOutPort outPort = mock(JourneyDistributionOutPort.class);
        when(outPort.existsById("UNKNOWN")).thenReturn(false);

        CheckJourneyDistributionStep step = new CheckJourneyDistributionStep(outPort);
        CreateQuestionnairePipelineContext context = contextWithJourneyId("UNKNOWN");

        Result<Void, List<DomainError>> result = step.execute(context);

        assertInstanceOf(Result.Failure.class, result);
        List<DomainError> errors = result.errorOrElseThrow(() -> new IllegalStateException("Expected failure"));
        assertTrue(errors.stream().anyMatch(e -> e.code().equals("JOURNEY_DISTRIBUTION_NOT_FOUND")));
    }

    private static CreateQuestionnairePipelineContext contextWithJourneyId(String journeyId) {
        var cmd = new CreateQuestionnaireCommand("q_001", "APP", journeyId, "Desc", null, null);
        return new CreateQuestionnairePipelineContext(cmd);
    }
}

