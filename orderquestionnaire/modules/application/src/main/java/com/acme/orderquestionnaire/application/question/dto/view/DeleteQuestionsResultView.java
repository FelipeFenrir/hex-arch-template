package com.acme.orderquestionnaire.application.question.dto.view;

import java.util.List;

public record DeleteQuestionsResultView(
        List<DeleteQuestionFailureView> failures
) {
    public DeleteQuestionsResultView {
        failures = failures == null ? List.of() : List.copyOf(failures);
    }

    public static DeleteQuestionsResultView empty() {
        return new DeleteQuestionsResultView(List.of());
    }
}

