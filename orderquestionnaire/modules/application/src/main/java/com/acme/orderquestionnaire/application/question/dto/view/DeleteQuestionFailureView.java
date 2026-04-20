package com.acme.orderquestionnaire.application.question.dto.view;

import java.util.List;

public record DeleteQuestionFailureView(
        String questionId,
        String code,
        String message,
        List<String> relatedQuestionnaireIds
) {
    public DeleteQuestionFailureView {
        relatedQuestionnaireIds = relatedQuestionnaireIds == null ? List.of() : List.copyOf(relatedQuestionnaireIds);
    }
}

