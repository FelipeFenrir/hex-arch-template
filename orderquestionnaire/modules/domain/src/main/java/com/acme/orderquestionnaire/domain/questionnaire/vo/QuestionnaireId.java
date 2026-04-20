package com.acme.orderquestionnaire.domain.questionnaire.vo;

import java.util.Objects;

public record QuestionnaireId(
        String id,
        String channelDistributionId,
        String journeyDistributionId
) {
    public QuestionnaireId {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(channelDistributionId, "channelDistributionId must not be null");
        Objects.requireNonNull(journeyDistributionId, "journeyDistributionId must not be null");
    }

    public static QuestionnaireId of(String id,
                                     String channelDistributionId,
                                     String journeyDistributionId) {
        return new QuestionnaireId(id, channelDistributionId, journeyDistributionId);
    }
}

