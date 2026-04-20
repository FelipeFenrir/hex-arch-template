package com.acme.orderquestionnaire.application.questionnaire.dto.view;

import java.util.List;

public record DeleteQuestionnairesResultView(
        List<DeleteQuestionnaireFailureView> failures
) {
    public DeleteQuestionnairesResultView {
        failures = failures == null ? List.of() : List.copyOf(failures);
    }

    public static DeleteQuestionnairesResultView empty() {
        return new DeleteQuestionnairesResultView(List.of());
    }
}

