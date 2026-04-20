package com.acme.shared.vo;

import java.time.LocalDateTime;

public interface AuditInfo {

    AuditUser createdBy();

    LocalDateTime createdAt();

    AuditUser updatedBy();

    LocalDateTime updatedAt();

    AuditInfo withUpdate(AuditUser updatedBy, LocalDateTime updatedAt);
}

