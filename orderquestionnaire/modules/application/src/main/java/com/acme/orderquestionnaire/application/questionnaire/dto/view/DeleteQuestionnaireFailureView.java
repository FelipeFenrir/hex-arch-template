package com.acme.orderquestionnaire.application.questionnaire.dto.view;

public record DeleteQuestionnaireFailureView(
        String id,
        String channelDistributionId,
        String journeyDistributionId,
        String code,
        String message
) {
}

