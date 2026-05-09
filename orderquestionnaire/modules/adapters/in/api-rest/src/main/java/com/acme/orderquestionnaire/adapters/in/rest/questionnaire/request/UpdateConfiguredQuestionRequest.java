package com.acme.orderquestionnaire.adapters.in.rest.questionnaire.request;

import com.acme.orderquestionnaire.application.questionnaire.dto.command.UpdateConfiguredQuestionParam;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

public record UpdateConfiguredQuestionRequest(
        @NotBlank String questionId,
        @Valid ConfiguredQuestionRequest param
) {
    public UpdateConfiguredQuestionParam toParam() {
        return new UpdateConfiguredQuestionParam(questionId, param == null ? null : param.toParam());
    }
}

