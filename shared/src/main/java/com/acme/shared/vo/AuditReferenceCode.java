package com.acme.shared.vo;

import java.util.Objects;

/**
 * Value Object representing an audit user's reference code.
 * Uniquely identifies a user in audit contexts for traceability.
 * Can be an employee ID, user code, or other identifying reference.
 */
public record AuditReferenceCode(String value) {
    public AuditReferenceCode {
        Objects.requireNonNull(value, "value must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("AuditReferenceCode value must not be blank");
        }
    }

    public static AuditReferenceCode of(String value) {
        return new AuditReferenceCode(value);
    }
}

