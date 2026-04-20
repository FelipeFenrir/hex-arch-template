package com.acme.orderquestionnaire.application.questionnaire.dto.command;

import java.util.Map;

public record ValidateQuestionnaireAnswersCommand(
        String questionnaireId,
        String channelDistributionId,
        String journeyDistributionId,
        Map<String, Object> answers
) {
}

