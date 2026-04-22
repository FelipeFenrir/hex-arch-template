package com.acme.orderquestionnaire.domain.questionnaire.answer;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@Builder(setterPrefix = "with", access = AccessLevel.PACKAGE, toBuilder = true)
public class AnswerOptionItem {
    private final String value;
    private final String label;

    public static AnswerOptionItem createNew(String value, String label) {
        return AnswerOptionItem.builder()
                .withValue(value)
                .withLabel(label)
                .build();
    }

    public static AnswerOptionItem rehydrate(String value, String label) {
        return AnswerOptionItem.builder()
                .withValue(value)
                .withLabel(label)
                .build();
    }

    public String value() {
        return this.value;
    }
    public String label() {
        return this.label;
    }
}
