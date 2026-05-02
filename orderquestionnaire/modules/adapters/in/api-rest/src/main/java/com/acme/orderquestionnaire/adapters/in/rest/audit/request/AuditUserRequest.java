package com.acme.orderquestionnaire.adapters.in.rest.audit.request;

import com.acme.orderquestionnaire.application.audit.dto.command.AuditUserParam;
import com.acme.shared.vo.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AuditUserRequest(
        @NotNull Id id,
        @NotBlank String referenceCode,
        @NotBlank String name,
        @NotBlank String email
) {
    public AuditUserParam toParam() {
        return new AuditUserParam(
                id,
                referenceCode,
                name,
                email
        );
    }
}
