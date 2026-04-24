package com.acme.orderquestionnaire.application.questionnaire.service.step;

import com.acme.orderquestionnaire.application.channel.port.out.ChannelDistributionOutPort;
import com.acme.orderquestionnaire.application.questionnaire.error.QuestionnaireErrors;
import com.acme.orderquestionnaire.application.questionnaire.service.context.CreateQuestionnairePipelineContext;
import com.acme.shared.pattern.pipeline.Step;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;

import java.util.List;
import java.util.Objects;

/**
 * Verifies that the requested channel distribution exists before attempting to create the questionnaire.
 *
 * <p>Read-only operation against {@link ChannelDistributionOutPort}; no side-effects.
 * Rollback is a no-op (default {@code NONE}).
 */
public class CheckChannelDistributionStep implements Step<CreateQuestionnairePipelineContext> {

    private final ChannelDistributionOutPort channelDistributionOutPort;

    public CheckChannelDistributionStep(ChannelDistributionOutPort channelDistributionOutPort) {
        this.channelDistributionOutPort = Objects.requireNonNull(channelDistributionOutPort,
                "channelDistributionOutPort must not be null");
    }

    @Override
    public String id() {
        return "CHECK_CHANNEL_DISTRIBUTION";
    }

    @Override
    public Result<Void, List<DomainError>> execute(CreateQuestionnairePipelineContext context) {
        String channelDistributionId = context.command().channelDistributionId();
        return channelDistributionOutPort.existsById(channelDistributionId)
                ? Result.success(null)
                : QuestionnaireErrors.CHANNEL_DISTRIBUTION_NOT_FOUND.asFailure(channelDistributionId);
    }
}

