package com.acme.orderquestionnaire.adapters.out.mongo.journey.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "journey_distributions")
public record JourneyDistributionEntity(
        @Id
        String id
) {}

