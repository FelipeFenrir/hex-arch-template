package com.acme.orderquestionnaire.application.questionnaire.service.support;

import com.acme.orderquestionnaire.domain.questionnaire.conditioner.QuestionCondition;

import java.util.Set;

public final class ConditionQuestionIdExtractor {

    private ConditionQuestionIdExtractor() {
        throw new IllegalStateException("Utility class");
    }

    public static Set<String> extract(QuestionCondition condition) {
        return condition == null ? Set.of() : condition.referencedQuestionIds();
    }
}

