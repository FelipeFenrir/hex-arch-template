package com.acme.orderquestionnaire.application.questionnaire.dto.view;

import java.util.Map;

public record ValidateQuestionnaireAnswersView(
        String questionnaireId,
        String channelDistributionId,
        String journeyDistributionId,
        boolean valid,
        Map<String, QuestionAnswerValidationView> violationsByQuestionId
) {
}

