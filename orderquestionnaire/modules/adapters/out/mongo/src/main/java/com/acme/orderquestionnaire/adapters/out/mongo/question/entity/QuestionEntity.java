package com.acme.orderquestionnaire.adapters.out.mongo.question.entity;

import com.acme.orderquestionnaire.adapters.out.mongo.audit.entity.AuditInfoDocument;
import com.acme.shared.enumerator.ParameterizationStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;


@Document(collection = "questions")
public record QuestionEntity(
        @Id
        String id,

        @Field("label")
        String label,

        @Field("status")
        ParameterizationStatus status,

        @Indexed
        @Field("sales_item_reference_code")
        String salesItemReferenceCode,

        @Field("audit_info")
        AuditInfoDocument auditInfo
) {}
