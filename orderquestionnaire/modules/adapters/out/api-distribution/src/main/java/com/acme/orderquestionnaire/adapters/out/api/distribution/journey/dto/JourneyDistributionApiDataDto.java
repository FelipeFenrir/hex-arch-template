package com.acme.orderquestionnaire.adapters.out.api.distribution.journey.dto;

public record JourneyDistributionApiDataDto(
        String id,
        String referenceCode,
        String name,
        boolean active
) {
}

