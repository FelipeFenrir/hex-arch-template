package com.acme.orderquestionnaire.adapters.out.mongo.audit.entity;

import org.springframework.data.mongodb.core.mapping.Field;

public record AuditUserDocument(
        @Field("id")
        String id,

        @Field("reference_code")
        String referenceCode,

        @Field("name")
        String name,

        @Field("email")
        String email
) {}

