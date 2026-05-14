package com.acme.orderquestionnaire.adapters.in.rest.questionnaire.request;

import com.acme.orderquestionnaire.application.questionnaire.dto.command.DeleteQuestionnaireCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record DeleteQuestionnairesRequest(
        @NotEmpty List<@Valid DeleteQuestionnaireItemRequest> items
) {
    public List<DeleteQuestionnaireCommand> toCommands() {
        return items.stream().map(DeleteQuestionnaireItemRequest::toCommand).toList();
    }
}

