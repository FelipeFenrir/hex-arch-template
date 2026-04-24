package com.acme.orderquestionnaire.adapters.out.api.distribution.channel.dto;

public record ChannelDistributionApiDataDto(
        String id,
        String referenceCode,
        String name,
        boolean active
) {
}

