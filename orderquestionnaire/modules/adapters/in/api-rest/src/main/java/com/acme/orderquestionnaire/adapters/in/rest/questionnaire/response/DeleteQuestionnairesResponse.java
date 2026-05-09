package com.acme.orderquestionnaire.adapters.in.rest.questionnaire.response;

import com.acme.orderquestionnaire.application.questionnaire.dto.view.DeleteQuestionnairesResultView;

import java.util.List;

public record DeleteQuestionnairesResponse(
        int requested,
        int failed,
        int deleted,
        List<DeleteQuestionnaireFailureResponse> failures
) {
    public DeleteQuestionnairesResponse {
        failures = failures == null ? List.of() : List.copyOf(failures);
    }

    public static DeleteQuestionnairesResponse from(DeleteQuestionnairesResultView view, int requested) {
        List<DeleteQuestionnaireFailureResponse> mappedFailures = view.failures().stream()
                .map(DeleteQuestionnaireFailureResponse::from)
                .toList();

        int failed = mappedFailures.size();
        return new DeleteQuestionnairesResponse(requested, failed, requested - failed, mappedFailures);
    }
}

