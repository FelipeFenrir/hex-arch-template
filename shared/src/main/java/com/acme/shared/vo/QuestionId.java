package com.acme.shared.vo;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value Object representing a question identification code.
 * Must follow snake_case format: lowercase letters with optional underscores and numbers.
 * Used as a semantic identifier for questions in questionnaires.
 * Example: "question_001", "standard_q_v1"
 */
public record QuestionId(String value) {
    private static final Pattern SNAKE_CASE_PATTERN = Pattern.compile("^[a-z]+(?:_[a-z0-9]+)*$");

    public QuestionId {
        Objects.requireNonNull(value, "value must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("QuestionId value must not be blank");
        }
        if (!SNAKE_CASE_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException(
                "QuestionId must follow snake_case format: " + value
            );
        }
    }

    public static QuestionId of(String value) {
        return new QuestionId(value);
    }
}

