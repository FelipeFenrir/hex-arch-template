package com.acme.orderquestionnaire.adapters.in.rest.questionnaire.response;

import com.acme.orderquestionnaire.application.questionnaire.dto.view.QuestionAnswerViolationView;

import java.util.Map;

public record QuestionAnswerViolationResponse(
        String code,
        String message,
        String source,
        String ruleType,
        Map<String, Object> ruleAttributes,
        String rulePath
) {
    public static QuestionAnswerViolationResponse from(QuestionAnswerViolationView view) {
        return new QuestionAnswerViolationResponse(
                view.code(),
                view.message(),
                view.source(),
                view.ruleType(),
                view.ruleAttributes(),
                view.rulePath()
        );
    }
}

