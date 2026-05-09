package com.acme.orderquestionnaire.adapters.in.rest.question.request;

import com.acme.orderquestionnaire.adapters.in.rest.audit.request.AuditUserRequest;
import com.acme.orderquestionnaire.application.question.dto.command.CreateQuestionCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CreateQuestionRequest(
        @NotBlank String id,
        @NotBlank String label,
        @NotBlank String salesItemReferenceCode,
        @Valid @NotNull AuditUserRequest createdBy
) {
    public CreateQuestionCommand toCommand() {
        return new CreateQuestionCommand(
                id,
                label,
                salesItemReferenceCode,
                createdBy.toParam(),
                LocalDateTime.now()
        );
    }
}
