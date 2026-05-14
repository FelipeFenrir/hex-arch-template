package com.acme.shared.vo;

import java.util.Objects;

/**
 * Value Object representing a sales item reference code.
 * Links questions to items in the sales/commerce system.
 * Must not be blank.
 */
public record SalesItemReferenceCode(String value) {
    public SalesItemReferenceCode {
        Objects.requireNonNull(value, "value must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("SalesItemReferenceCode value must not be blank");
        }
    }

    public static SalesItemReferenceCode of(String value) {
        return new SalesItemReferenceCode(value);
    }
}

