package com.acme.orderquestionnaire.adapters.in.rest.questionnaire.response;

import com.acme.orderquestionnaire.application.questionnaire.dto.view.DeleteQuestionnaireFailureView;

public record DeleteQuestionnaireFailureResponse(
        String id,
        String channelId,
        String journeyId,
        String code,
        String message
) {
    public static DeleteQuestionnaireFailureResponse from(DeleteQuestionnaireFailureView view) {
        return new DeleteQuestionnaireFailureResponse(
                view.id(),
                view.channelDistributionId(),
                view.journeyDistributionId(),
                view.code(),
                view.message()
        );
    }
}

