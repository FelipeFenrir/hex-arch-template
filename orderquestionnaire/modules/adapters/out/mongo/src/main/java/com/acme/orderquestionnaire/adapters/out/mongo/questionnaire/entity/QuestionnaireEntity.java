package com.acme.orderquestionnaire.adapters.out.mongo.questionnaire.entity;

import com.acme.orderquestionnaire.adapters.out.mongo.audit.entity.AuditInfoDocument;
import com.acme.shared.enumerator.ParameterizationStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "questionnaires")
public record QuestionnaireEntity(
        @Id
        String documentId,

        @Indexed
        @Field("id")
        String id,

        @Indexed
        @Field("channel_distribution_id")
        String channelDistributionId,

        @Indexed
        @Field("journey_distribution_id")
        String journeyDistributionId,

        @Field("description")
        String description,

        @Field("status")
        ParameterizationStatus status,

        @Field("audit_info")
        AuditInfoDocument auditInfo,

        @Field("questions_count")
        Integer questionsCount
) {}

