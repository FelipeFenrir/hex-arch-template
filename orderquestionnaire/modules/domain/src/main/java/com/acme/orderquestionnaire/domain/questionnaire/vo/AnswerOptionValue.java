package com.acme.orderquestionnaire.domain.questionnaire.vo;

import java.util.Objects;

/**
 * Value Object representing the value of an answer option.
 * Used to identify selectable options in OPTION_LIST answer configurations.
 * Must not be blank; typically a code or identifier for the option.
 */
public record AnswerOptionValue(String value) {
    public AnswerOptionValue {
        Objects.requireNonNull(value, "value must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("AnswerOptionValue must not be blank");
        }
    }

    public static AnswerOptionValue of(String value) {
        return new AnswerOptionValue(value);
    }
}

