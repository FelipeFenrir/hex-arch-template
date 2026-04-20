package com.acme.orderquestionnaire.domain.audit;

import com.acme.shared.vo.Id;
import com.acme.shared.vo.AuditUser;

public record OrderQuestionnaireAuditUser(
        Id id,
        String referenceCode,
        String name,
        String email
) implements AuditUser {

    public OrderQuestionnaireAuditUser {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null or blank");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be null or blank");
        }
    }
}

