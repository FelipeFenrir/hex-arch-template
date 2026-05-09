package com.acme.orderquestionnaire.adapters.in.rest.question.response;

import com.acme.orderquestionnaire.application.question.dto.view.DeleteQuestionFailureView;

import java.util.List;

public record DeleteQuestionFailureResponse(
        String questionId,
        String code,
        String message,
        List<String> relatedQuestionnaireIds
) {
    public DeleteQuestionFailureResponse {
        relatedQuestionnaireIds = relatedQuestionnaireIds == null ? List.of() : List.copyOf(relatedQuestionnaireIds);
    }

    public static DeleteQuestionFailureResponse from(DeleteQuestionFailureView view) {
        return new DeleteQuestionFailureResponse(
                view.questionId(),
                view.code(),
                view.message(),
                view.relatedQuestionnaireIds()
        );
    }
}

