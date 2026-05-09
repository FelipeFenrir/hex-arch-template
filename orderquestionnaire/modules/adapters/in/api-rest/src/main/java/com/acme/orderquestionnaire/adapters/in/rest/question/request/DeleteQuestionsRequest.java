package com.acme.orderquestionnaire.adapters.in.rest.question.request;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record DeleteQuestionsRequest(@NotEmpty List<String> ids) {
    public DeleteQuestionsRequest {
        ids = ids == null ? List.of() : List.copyOf(ids);
    }
}

