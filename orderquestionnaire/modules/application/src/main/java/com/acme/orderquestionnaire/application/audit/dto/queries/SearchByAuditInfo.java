package com.acme.orderquestionnaire.application.audit.dto.queries;

import java.time.LocalDateTime;

public record SearchByAuditInfo(
        String createdById,
        String createdByReferenceCode,
        String createdByNameContains,
        String createdByEmailContains,
        LocalDateTime createdAtStart,
        LocalDateTime createdAtEnd,
        String updatedById,
        String updatedByReferenceCode,
        String updatedByNameContains,
        String updatedByEmailContains,
        LocalDateTime updatedAtStart,
        LocalDateTime updatedAtEnd
) { }
