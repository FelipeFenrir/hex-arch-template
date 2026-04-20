package com.acme.orderquestionnaire.adapters.out.mongo.audit.entity;

import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

public record AuditInfoDocument(
        @Field("created_by")
        AuditUserDocument createdBy,

        @Field("created_at")
        LocalDateTime createdAt,

        @Field("updated_by")
        AuditUserDocument updatedBy,

        @Field("updated_at")
        LocalDateTime updatedAt
) {}

