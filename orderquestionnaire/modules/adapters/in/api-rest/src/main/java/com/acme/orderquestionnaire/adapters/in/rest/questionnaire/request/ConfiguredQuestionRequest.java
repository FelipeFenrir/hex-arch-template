package com.acme.orderquestionnaire.adapters.in.rest.questionnaire.request;

import com.acme.orderquestionnaire.application.questionnaire.dto.command.ConfiguredQuestionParam;
import jakarta.validation.Valid;

public record ConfiguredQuestionRequest(
        Integer order,
        @Valid AnswerConfigRequest answerConfiguration,
        @Valid ConditionRequest rootCondition
) {
    public ConfiguredQuestionParam toParam() {
        return new ConfiguredQuestionParam(
                order,
                answerConfiguration == null ? null : answerConfiguration.toParam(),
                rootCondition == null ? null : rootCondition.toParam()
        );
    }
}

