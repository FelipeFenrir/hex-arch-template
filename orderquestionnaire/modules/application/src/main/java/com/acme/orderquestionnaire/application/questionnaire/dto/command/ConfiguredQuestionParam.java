package com.acme.orderquestionnaire.application.questionnaire.dto.command;

public record ConfiguredQuestionParam(
        Integer order,
        AnswerConfigParam answerConfig,
        ConditionParam rootCondition
) { }

