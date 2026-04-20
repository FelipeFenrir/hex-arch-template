package com.acme.orderquestionnaire.domain.questionnaire.conditioner;

import java.util.ArrayList;
import java.util.List;

public class CompositeConditionBuilder {
    private final List<QuestionCondition> conditions;
    private final boolean isAnd;

    public static CompositeConditionBuilder and(QuestionCondition condition) {
        return new CompositeConditionBuilder(condition, true);
    }

    public static CompositeConditionBuilder or(QuestionCondition condition) {
        return new CompositeConditionBuilder(condition, false);
    }

    private CompositeConditionBuilder(QuestionCondition condition, boolean isAnd) {
        this.conditions = new ArrayList<>();
        this.conditions.add(condition);
        this.isAnd = isAnd;
    }

    public CompositeConditionBuilder add(QuestionCondition condition) {
        this.conditions.add(condition);
        return this;
    }

    public QuestionCondition build() {
        if (conditions.isEmpty()) {
            return new CompositeCondition(true); // Sem condições = sempre satisfeito
        }

        if (conditions.size() == 1) {
            return conditions.getFirst(); // Se apenas uma condição, retorna ela mesma
        }

        CompositeCondition composite = new CompositeCondition(isAnd);
        conditions.forEach(composite::addCondition);
        return composite;
    }

    public int size() {
        return conditions.size();
    }

    public boolean isAndOperator() {
        return isAnd;
    }
}



