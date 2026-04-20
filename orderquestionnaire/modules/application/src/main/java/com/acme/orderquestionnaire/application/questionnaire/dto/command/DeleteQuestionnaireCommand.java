package com.acme.orderquestionnaire.application.questionnaire.dto.command;

public record DeleteQuestionnaireCommand(
        String id,
        String channelDistributionId,
        String journeyDistributionId
) {
}

