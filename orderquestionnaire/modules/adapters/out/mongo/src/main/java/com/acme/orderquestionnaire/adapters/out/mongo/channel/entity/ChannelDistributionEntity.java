package com.acme.orderquestionnaire.adapters.out.mongo.channel.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "channel_distributions")
public record ChannelDistributionEntity(
        @Id
        String id
) {}

