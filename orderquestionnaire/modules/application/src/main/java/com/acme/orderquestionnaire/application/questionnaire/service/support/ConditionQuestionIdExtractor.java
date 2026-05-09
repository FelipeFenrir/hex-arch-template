package com.acme.orderquestionnaire.application.questionnaire.service.support;

import com.acme.orderquestionnaire.domain.questionnaire.conditioner.QuestionCondition;
import com.acme.shared.vo.QuestionId;

import java.util.Set;
import java.util.stream.Collectors;

public final class ConditionQuestionIdExtractor {

    private ConditionQuestionIdExtractor() {
        throw new IllegalStateException("Utility class");
    }

    public static Set<String> extract(QuestionCondition condition) {
        return condition == null
                ? Set.of()
                : condition.referencedQuestionIds().stream()
                        .map(QuestionId::value)
                        .collect(Collectors.toUnmodifiableSet());
    }
}

