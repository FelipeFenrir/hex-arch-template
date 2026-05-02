package com.acme.orderquestionnaire.adapters.in.rest.audit.response;

import com.acme.orderquestionnaire.application.audit.dto.view.UserView;

public record AuditUserResponse(
        String id,
        String referenceCode,
        String name,
        String email
) {
    public static AuditUserResponse from(UserView auditUserView) {
        return new AuditUserResponse(
                auditUserView.id(),
                auditUserView.referenceCode(),
                auditUserView.name(),
                auditUserView.email()
        );
    }
}
