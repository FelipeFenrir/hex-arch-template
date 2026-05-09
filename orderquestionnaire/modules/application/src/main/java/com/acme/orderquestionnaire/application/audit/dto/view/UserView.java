package com.acme.orderquestionnaire.application.audit.dto.view;

import com.acme.shared.vo.AuditUser;

public record UserView(
        String id,
        String referenceCode,
        String name,
        String email
) {

    public static UserView from(AuditUser auditUser) {
        if (auditUser == null) {
            return null;
        }
        return new UserView(
                auditUser.id().stringfyId(),
                auditUser.referenceCode(),
                auditUser.name(),
                auditUser.email()
        );
    }
}

