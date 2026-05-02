package com.acme.orderquestionnaire.adapters.in.rest.question.request;

import com.acme.orderquestionnaire.application.audit.dto.command.AuditUserParam;
import com.acme.orderquestionnaire.application.question.dto.command.CreateQuestionCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CreateQuestionRequest(
        @NotBlank String id,
        @NotBlank String label,
        @NotBlank String salesItemReferenceCode
) {
    public CreateQuestionCommand toCommand() {
        return new CreateQuestionCommand(
                id,
                label,
                salesItemReferenceCode,
                createdBy,
                LocalDateTime.now()
        );
    }
}
