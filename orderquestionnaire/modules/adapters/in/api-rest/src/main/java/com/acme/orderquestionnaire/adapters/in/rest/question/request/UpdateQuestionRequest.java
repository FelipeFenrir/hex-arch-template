package com.acme.orderquestionnaire.adapters.in.rest.question.request;

import com.acme.orderquestionnaire.adapters.in.rest.audit.request.AuditUserRequest;
import com.acme.orderquestionnaire.application.question.dto.command.UpdateQuestionCommand;
import com.acme.shared.enumerator.ParameterizationStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record UpdateQuestionRequest(
        @NotBlank String label,
        @NotBlank String salesItemReferenceCode,
        @NotNull ParameterizationStatus status,
        @Valid @NotNull AuditUserRequest updatedBy
) {
    public UpdateQuestionCommand toCommand() {
        return new UpdateQuestionCommand(
                label,
                salesItemReferenceCode,
                status,
                updatedBy.toParam(),
                LocalDateTime.now()
        );
    }
}

