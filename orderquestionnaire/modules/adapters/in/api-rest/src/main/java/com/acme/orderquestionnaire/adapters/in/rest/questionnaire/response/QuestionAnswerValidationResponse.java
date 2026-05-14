package com.acme.orderquestionnaire.adapters.in.rest.questionnaire.response;

import com.acme.orderquestionnaire.application.questionnaire.dto.view.QuestionAnswerValidationView;

import java.util.List;

public record QuestionAnswerValidationResponse(
        String questionId,
        String questionLabel,
        Integer order,
        Object providedAnswer,
        List<QuestionAnswerViolationResponse> violations
) {
    public static QuestionAnswerValidationResponse from(QuestionAnswerValidationView view) {
        List<QuestionAnswerViolationResponse> violationResponses = view.violations() == null
                ? List.of()
                : view.violations().stream()
                        .map(QuestionAnswerViolationResponse::from)
                        .toList();

        return new QuestionAnswerValidationResponse(
                view.questionId(),
                view.questionLabel(),
                view.order(),
                view.providedAnswer(),
                violationResponses
        );
    }
}

