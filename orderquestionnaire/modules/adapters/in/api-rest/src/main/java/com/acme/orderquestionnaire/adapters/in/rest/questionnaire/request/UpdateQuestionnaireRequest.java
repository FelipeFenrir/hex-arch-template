package com.acme.orderquestionnaire.adapters.in.rest.questionnaire.request;

import com.acme.orderquestionnaire.adapters.in.rest.audit.request.AuditUserRequest;
import com.acme.orderquestionnaire.application.questionnaire.dto.command.UpdateQuestionnaireCommand;
import com.acme.orderquestionnaire.application.questionnaire.dto.command.UpdateConfiguredQuestionParam;
import com.acme.shared.enumerator.ParameterizationStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public record UpdateQuestionnaireRequest(
        String description,
        ParameterizationStatus status,
        List<@Valid UpdateConfiguredQuestionRequest> questionsToUpsert,
        List<String> questionIdsToRemove,
        @Valid @NotNull AuditUserRequest updatedBy
) {
    public UpdateQuestionnaireCommand toCommand(String id, String channelId, String journeyId) {
        List<UpdateConfiguredQuestionParam> upserts = questionsToUpsert == null
                ? List.of()
                : questionsToUpsert.stream().map(UpdateConfiguredQuestionRequest::toParam).toList();

        return new UpdateQuestionnaireCommand(
                id,
                channelId,
                journeyId,
                description,
                status,
                upserts,
                questionIdsToRemove == null ? List.of() : List.copyOf(questionIdsToRemove),
                updatedBy.toParam(),
                LocalDateTime.now()
        );
    }
}

