package com.acme.orderquestionnaire.adapters.out.mongo.channel.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "channel_distributions")
public record ChannelDistributionEntity(
        @Id
        String id,

        @Field("reference_code")
        String referenceCode,

        @Field("name")
        String name,

        @Field("active")
        boolean active
) {}

