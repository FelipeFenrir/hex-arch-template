package com.acme.shared.vo;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value Object representing a questionnaire identification code.
 * Must follow snake_case format: lowercase letters with optional underscores and numbers.
 * Example: "q_001", "questionnaire_v1"
 */
public record QuestionnaireCode(String value) {
    private static final Pattern SNAKE_CASE_PATTERN = Pattern.compile("^[a-z]+(?:_[a-z0-9]+)*$");

    public QuestionnaireCode {
        Objects.requireNonNull(value, "value must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("QuestionnaireCode value must not be blank");
        }
        if (!SNAKE_CASE_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException(
                "QuestionnaireCode must follow snake_case format: " + value
            );
        }
    }

    public static QuestionnaireCode of(String value) {
        return new QuestionnaireCode(value);
    }
}

