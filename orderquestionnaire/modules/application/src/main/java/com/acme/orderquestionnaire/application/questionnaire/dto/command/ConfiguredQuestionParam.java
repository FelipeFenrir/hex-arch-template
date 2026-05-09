package com.acme.orderquestionnaire.application.questionnaire.dto.command;

public record ConfiguredQuestionParam(
        Integer order,
        AnswerConfigParam answerConfiguration,
        ConditionParam rootCondition
) { }

