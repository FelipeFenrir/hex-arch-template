package com.acme.orderquestionnaire.application.questionnaire.service.step;

import com.acme.orderquestionnaire.application.journey.port.out.JourneyDistributionOutPort;
import com.acme.orderquestionnaire.application.questionnaire.error.QuestionnaireErrors;
import com.acme.orderquestionnaire.application.questionnaire.service.context.CreateQuestionnairePipelineContext;
import com.acme.shared.pattern.pipeline.Step;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;

import java.util.List;
import java.util.Objects;

/**
 * Verifies that the requested journey distribution exists before attempting to create the questionnaire.
 *
 * <p>Read-only operation against {@link JourneyDistributionOutPort}; no side-effects.
 * Rollback is a no-op (default {@code NONE}).
 */
public class CheckJourneyDistributionStep implements Step<CreateQuestionnairePipelineContext> {

    private final JourneyDistributionOutPort journeyDistributionOutPort;

    public CheckJourneyDistributionStep(JourneyDistributionOutPort journeyDistributionOutPort) {
        this.journeyDistributionOutPort = Objects.requireNonNull(journeyDistributionOutPort,
                "journeyDistributionOutPort must not be null");
    }

    @Override
    public String id() {
        return "CHECK_JOURNEY_DISTRIBUTION";
    }

    @Override
    public Result<Void, List<DomainError>> execute(CreateQuestionnairePipelineContext context) {
        String journeyDistributionId = context.command().journeyDistributionId();
        return journeyDistributionOutPort.existsById(journeyDistributionId)
                ? Result.success(null)
                : QuestionnaireErrors.JOURNEY_DISTRIBUTION_NOT_FOUND.asFailure(journeyDistributionId);
    }
}

