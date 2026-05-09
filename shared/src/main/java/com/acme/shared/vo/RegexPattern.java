package com.acme.shared.vo;

import java.util.Objects;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Value Object representing a validated regular expression pattern.
 * Validates regex syntax at construction time and caches the compiled Pattern for performance.
 * Used for text answer validation.
 */
public record RegexPattern(String value, Pattern compiled) {
    public RegexPattern(String value) {
        this(value, compilePattern(value));
    }

    public RegexPattern {
        Objects.requireNonNull(value, "value must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("RegexPattern value must not be blank");
        }
        Objects.requireNonNull(compiled, "compiled pattern must not be null");
    }

    private static Pattern compilePattern(String pattern) {
        if (pattern == null || pattern.isBlank()) {
            throw new IllegalArgumentException("RegexPattern value must not be blank");
        }
        try {
            return Pattern.compile(pattern);
        } catch (PatternSyntaxException e) {
            throw new IllegalArgumentException(
                "Invalid regex pattern: " + e.getMessage(), e
            );
        }
    }

    public static RegexPattern of(String value) {
        return new RegexPattern(value);
    }

    /**
     * Returns the compiled Pattern object for efficient matching.
     */
    public Pattern asPattern() {
        return compiled;
    }
}

