package com.acme.orderquestionnaire.adapters.in.rest.questionnaire.request;

import com.acme.orderquestionnaire.application.questionnaire.dto.command.DeleteQuestionnaireCommand;
import jakarta.validation.constraints.NotBlank;

public record DeleteQuestionnaireItemRequest(
        @NotBlank String id,
        @NotBlank String channelId,
        @NotBlank String journeyId
) {
    public DeleteQuestionnaireCommand toCommand() {
        return new DeleteQuestionnaireCommand(id, channelId, journeyId);
    }
}

