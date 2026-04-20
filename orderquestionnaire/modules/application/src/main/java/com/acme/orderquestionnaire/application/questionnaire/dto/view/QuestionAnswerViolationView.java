package com.acme.orderquestionnaire.application.questionnaire.dto.view;

import java.util.Map;

public record QuestionAnswerViolationView(
        String code,
        String message,
        String source,
        String ruleType,
        Map<String, Object> ruleAttributes,
        String rulePath
) {
}
