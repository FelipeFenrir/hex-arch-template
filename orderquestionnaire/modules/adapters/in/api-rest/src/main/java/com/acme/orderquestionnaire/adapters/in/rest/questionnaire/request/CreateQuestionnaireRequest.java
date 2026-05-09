package com.acme.orderquestionnaire.adapters.in.rest.questionnaire.request;

import com.acme.orderquestionnaire.adapters.in.rest.audit.request.AuditUserRequest;
import com.acme.orderquestionnaire.application.questionnaire.dto.command.CreateQuestionnaireCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CreateQuestionnaireRequest(
        @NotBlank String id,
        @NotBlank String channelDistributionId,
        @NotBlank String journeyDistributionId,
        @NotBlank String description,
        @Valid @NotNull AuditUserRequest createdBy
) {
    public CreateQuestionnaireCommand toCommand() {
        return new CreateQuestionnaireCommand(
                id,
                channelDistributionId,
                journeyDistributionId,
                description,
                createdBy.toParam(),
                LocalDateTime.now()
        );
    }
}

