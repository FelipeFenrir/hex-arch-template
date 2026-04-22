package com.acme.orderquestionnaire.domain.questionnaire.conditioner;

import java.util.Objects;

public final class QuestionConditionComposer {

    private QuestionConditionComposer() {
        throw new IllegalStateException("Utility class");
    }

    public static Composer condition(QuestionCondition first) {
        return new Composer(first);
    }

    public static QuestionCondition composeWithAnd(QuestionCondition... conditions) {
        return combineConditions(true, conditions);
    }

    public static QuestionCondition composeWithOr(QuestionCondition... conditions) {
        return combineConditions(false, conditions);
    }

    public static QuestionCondition combineConditions(boolean isAnd, QuestionCondition... conditions) {
        if (conditions == null || conditions.length == 0) {
            return null;
        }
        if (conditions.length == 1) {
            return wrapCondition(conditions[0]);
        }

        QuestionCondition result = wrapCondition(conditions[0]);
        for (int i = 1; i < conditions.length; i++) {
            result = merge(result, conditions[i], isAnd);
        }
        return result;
    }

    public static final class Composer {
        public static final String FIRST_CONDITION_MUST_NOT_BE_NULL = "first condition must not be null";
        public static final String CONDITION_MUST_NOT_BE_NULL = "condition must not be null";
        private QuestionCondition current;

        private Composer(QuestionCondition first) {
            this.current = wrapCondition(Objects.requireNonNull(first, FIRST_CONDITION_MUST_NOT_BE_NULL));
        }

        public Composer and(QuestionCondition other) {
            Objects.requireNonNull(other, CONDITION_MUST_NOT_BE_NULL);
            this.current = merge(this.current, other, true);
            return this;
        }

        public Composer or(QuestionCondition other) {
            Objects.requireNonNull(other, CONDITION_MUST_NOT_BE_NULL);
            this.current = merge(this.current, other, false);
            return this;
        }

        public QuestionCondition build() {
            return this.current;
        }
    }

    private static QuestionCondition merge(QuestionCondition left, QuestionCondition right, boolean isAnd) {
        CompositeCondition composite = new CompositeCondition(isAnd);
        composite.addCondition(left);
        composite.addCondition(right);
        return composite;
    }

    private static QuestionCondition wrapCondition(QuestionCondition condition) {
        if (condition instanceof CompositeCondition) {
            return condition;
        }
        CompositeCondition composite = new CompositeCondition(true);
        composite.addCondition(condition);
        return composite;
    }
}

