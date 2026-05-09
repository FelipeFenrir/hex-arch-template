package com.acme.orderquestionnaire.domain.audit;

import com.acme.shared.vo.Id;
import com.acme.shared.vo.AuditUser;
import com.acme.shared.vo.AuditReferenceCode;
import com.acme.shared.vo.EmailAddress;

/**
 * Domain implementation of AuditUser interface.
 * Uses Value Objects for email and reference code to ensure type safety and validation.
 */
public record OrderQuestionnaireAuditUser(
        Id id,
        AuditReferenceCode auditReferenceCode,
        String name,
        EmailAddress emailAddress
) implements AuditUser {

    public OrderQuestionnaireAuditUser {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null or blank");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be null or blank");
        }
        if (auditReferenceCode == null) {
            throw new IllegalArgumentException("auditReferenceCode must not be null");
        }
    }

    /**
     * Backward compatibility accessor from AuditUser interface.
     * Returns the String value of the reference code.
     * Used at adapter boundaries to maintain API contracts.
     */
    @Override
    public String referenceCode() {
        return auditReferenceCode.value();
    }

    /**
     * Backward compatibility accessor from AuditUser interface.
     * Returns the String value of the email.
     * Used at adapter boundaries to maintain API contracts.
     */
    @Override
    public String email() {
        return emailAddress == null ? null : emailAddress.value();
    }
}

