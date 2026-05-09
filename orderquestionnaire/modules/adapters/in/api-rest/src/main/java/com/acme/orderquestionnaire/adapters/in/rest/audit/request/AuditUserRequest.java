package com.acme.orderquestionnaire.adapters.in.rest.audit.request;

import com.acme.orderquestionnaire.application.audit.dto.command.AuditUserParam;
import com.acme.shared.vo.Id;
import jakarta.validation.constraints.NotBlank;

public record AuditUserRequest(
        @NotBlank String id,
        @NotBlank String referenceCode,
        @NotBlank String name,
        @NotBlank String email
) {
    public AuditUserParam toParam() {
        return new AuditUserParam(
                Id.withId(id),
                referenceCode,
                name,
                email
        );
    }
}
