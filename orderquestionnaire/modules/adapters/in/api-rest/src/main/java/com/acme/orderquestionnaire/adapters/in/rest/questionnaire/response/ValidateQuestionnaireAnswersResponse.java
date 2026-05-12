package com.acme.orderquestionnaire.adapters.in.rest.questionnaire.response;

import com.acme.orderquestionnaire.application.questionnaire.dto.view.ValidateQuestionnaireAnswersView;

import java.util.Map;

public record ValidateQuestionnaireAnswersResponse(
        String questionnaireId,
        String channelDistributionId,
        String journeyDistributionId,
        boolean valid,
        Map<String, QuestionAnswerValidationResponse> violationsByQuestionId
) {
    public static ValidateQuestionnaireAnswersResponse from(ValidateQuestionnaireAnswersView view) {
        Map<String, QuestionAnswerValidationResponse> violations = view.violationsByQuestionId() == null
                ? Map.of()
                : view.violationsByQuestionId().entrySet().stream()
                        .collect(
                                java.util.LinkedHashMap::new,
                                (map, entry) -> map.put(entry.getKey(), QuestionAnswerValidationResponse.from(entry.getValue())),
                                Map::putAll
                        );

        return new ValidateQuestionnaireAnswersResponse(
                view.questionnaireId(),
                view.channelDistributionId(),
                view.journeyDistributionId(),
                view.valid(),
                violations
        );
    }
}

