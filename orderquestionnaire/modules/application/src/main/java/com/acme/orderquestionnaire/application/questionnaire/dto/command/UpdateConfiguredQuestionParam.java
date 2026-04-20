package com.acme.orderquestionnaire.application.questionnaire.dto.command;

public record UpdateConfiguredQuestionParam(
        String questionId,
        ConfiguredQuestionParam param
) { }

