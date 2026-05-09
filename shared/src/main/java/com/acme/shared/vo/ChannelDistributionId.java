package com.acme.shared.vo;

import java.util.Objects;

/**
 * Value Object representing a channel distribution identifier.
 * Used to reference a channel in the distribution system.
 */
public record ChannelDistributionId(String value) {
    public ChannelDistributionId {
        Objects.requireNonNull(value, "value must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("ChannelDistributionId value must not be blank");
        }
    }

    public static ChannelDistributionId of(String value) {
        return new ChannelDistributionId(value);
    }
}

