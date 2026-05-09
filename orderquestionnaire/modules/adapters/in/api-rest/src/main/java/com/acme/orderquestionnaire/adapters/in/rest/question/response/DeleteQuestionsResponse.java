package com.acme.orderquestionnaire.adapters.in.rest.question.response;

import com.acme.orderquestionnaire.application.question.dto.view.DeleteQuestionsResultView;

import java.util.List;

public record DeleteQuestionsResponse(
        int requested,
        int failed,
        int deleted,
        List<DeleteQuestionFailureResponse> failures
) {
    public DeleteQuestionsResponse {
        failures = failures == null ? List.of() : List.copyOf(failures);
    }

    public static DeleteQuestionsResponse from(DeleteQuestionsResultView view, int requested) {
        List<DeleteQuestionFailureResponse> mappedFailures = view.failures().stream()
                .map(DeleteQuestionFailureResponse::from)
                .toList();

        int failed = mappedFailures.size();
        return new DeleteQuestionsResponse(
                requested,
                failed,
                requested - failed,
                mappedFailures
        );
    }
}

