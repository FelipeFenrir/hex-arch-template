package com.acme.orderquestionnaire.adapters.in.rest.question.response;

public record DeleteQuestionResponse(
        String id,
        boolean deleted
) {
    public static DeleteQuestionResponse success(String id) {
        return new DeleteQuestionResponse(id, true);
    }
}

