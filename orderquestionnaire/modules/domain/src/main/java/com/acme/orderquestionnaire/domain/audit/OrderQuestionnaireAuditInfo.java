package com.acme.orderquestionnaire.domain.audit;

import com.acme.shared.vo.AuditInfo;
import com.acme.shared.vo.AuditUser;

import java.time.LocalDateTime;

public record OrderQuestionnaireAuditInfo(
        AuditUser createdBy,
        LocalDateTime createdAt,
        AuditUser updatedBy,
        LocalDateTime updatedAt
) implements AuditInfo {

    public OrderQuestionnaireAuditInfo {
        if (createdBy == null) {
            throw new IllegalArgumentException("createdBy must not be null");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("createdAt must not be null");
        }
        if ((updatedBy == null) != (updatedAt == null)) {
            throw new IllegalArgumentException("updatedBy and updatedAt must be provided together");
        }
    }

    @Override
    public AuditInfo withUpdate(AuditUser updatedBy, LocalDateTime updatedAt) {
        return new OrderQuestionnaireAuditInfo(createdBy, createdAt, updatedBy, updatedAt);
    }
}

