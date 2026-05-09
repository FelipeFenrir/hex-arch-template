package com.acme.shared.vo;

import java.util.Objects;

/**
 * Value Object representing a journey distribution identifier.
 * Used to reference a journey in the distribution system.
 */
public record JourneyDistributionId(String value) {
    public JourneyDistributionId {
        Objects.requireNonNull(value, "value must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("JourneyDistributionId value must not be blank");
        }
    }

    public static JourneyDistributionId of(String value) {
        return new JourneyDistributionId(value);
    }
}

