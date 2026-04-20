package com.acme.orderquestionnaire.application.questionnaire.dto.view;

import java.util.List;
import java.util.Set;

public record QuestionAnswerValidationView(
        String questionId,
        String questionLabel,
        int order,
        Object providedAnswer,
        boolean visibleByCondition,
        AnswerConfigurationView answerRule,
        QuestionConditionView conditionRule,
        Set<String> dependsOnQuestionIds,
        List<QuestionAnswerViolationView> violations
) {
}

