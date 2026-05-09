package com.acme.shared.vo;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value Object representing an email address.
 * Validates basic email format. Used for audit user communication and notification.
 * Can be used across multiple modules (audit, notifications, etc.).
 */
public record EmailAddress(String value) {
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    public EmailAddress {
        Objects.requireNonNull(value, "value must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("EmailAddress value must not be blank");
        }
        if (!EMAIL_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException(
                "Invalid email address format: " + value
            );
        }
    }

    public static EmailAddress of(String value) {
        return new EmailAddress(value);
    }
}

