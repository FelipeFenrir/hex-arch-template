package com.acme.orderquestionnaire.adapters.out.mongo.journey.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "journey_distributions")
public record JourneyDistributionEntity(
        @Id
        String id,

        @Field("reference_code")
        String referenceCode,

        @Field("name")
        String name,

        @Field("active")
        boolean active
) {}

