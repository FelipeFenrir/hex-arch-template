package com.acme.orderquestionnaire.domain.questionnaire.vo;

import com.acme.shared.vo.QuestionnaireCode;
import com.acme.shared.vo.ChannelDistributionId;
import com.acme.shared.vo.JourneyDistributionId;

import java.util.Objects;

/**
 * Composite Value Object representing the complete questionnaire identity.
 * Immutable record combining questionnaire code, channel distribution, and journey distribution IDs.
 */
public record QuestionnaireId(
        QuestionnaireCode questionnaireCode,
        ChannelDistributionId channelDistributionId,
        JourneyDistributionId journeyDistributionId
) {
    public QuestionnaireId {
        Objects.requireNonNull(questionnaireCode, "questionnaireCode must not be null");
        Objects.requireNonNull(channelDistributionId, "channelDistributionId must not be null");
        Objects.requireNonNull(journeyDistributionId, "journeyDistributionId must not be null");
    }

    public static QuestionnaireId of(String id,
                                     String channelDistributionId,
                                     String journeyDistributionId) {
        return new QuestionnaireId(
            QuestionnaireCode.of(id),
            ChannelDistributionId.of(channelDistributionId),
            JourneyDistributionId.of(journeyDistributionId)
        );
    }

    /**
     * Backward compatibility accessor: returns the String value of the questionnaire code.
     * Used at adapter boundaries to maintain API contracts.
     */
    public String id() {
        return questionnaireCode.value();
    }

    /**
     * Returns the channel distribution value as String for backward compatibility.
     */
    public String getChannelDistributionIdValue() {
        return this.channelDistributionId.value();
    }

    /**
     * Returns the journey distribution value as String for backward compatibility.
     */
    public String getJourneyDistributionIdValue() {
        return this.journeyDistributionId.value();
    }
}

