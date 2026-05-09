package com.acme.orderquestionnaire.adapters.in.rest.questionnaire.response;

public record DeleteQuestionnaireResponse(
        String id,
        String channelId,
        String journeyId,
        boolean deleted
) {
    public static DeleteQuestionnaireResponse success(String id, String channelId, String journeyId) {
        return new DeleteQuestionnaireResponse(id, channelId, journeyId, true);
    }
}

