package com.acme.orderquestionnaire.domain.questionnaire.vo;

/**
 * Value Object representing the order position of a question within a questionnaire.
 * Must be non-negative (>= 0). Used for sorting and presenting questions in sequence.
 */
public record QuestionOrder(int value) {
    public QuestionOrder {
        if (value < 0) {
            throw new IllegalArgumentException("QuestionOrder value must be >= 0, got: " + value);
        }
    }

    public static QuestionOrder of(int value) {
        return new QuestionOrder(value);
    }

    /**
     * Returns the next order value.
     */
    public QuestionOrder next() {
        return new QuestionOrder(value + 1);
    }

    /**
     * Increments this order by the given amount.
     */
    public QuestionOrder incrementBy(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Increment amount must not be negative: " + amount);
        }
        return new QuestionOrder(value + amount);
    }
}

