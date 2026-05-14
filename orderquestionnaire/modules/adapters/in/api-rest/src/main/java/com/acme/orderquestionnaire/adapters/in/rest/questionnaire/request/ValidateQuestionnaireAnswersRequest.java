package com.acme.orderquestionnaire.adapters.in.rest.questionnaire.request;

import com.acme.orderquestionnaire.application.questionnaire.dto.command.ValidateQuestionnaireAnswersCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record ValidateQuestionnaireAnswersRequest(
        @NotBlank String questionnaireId,
        @NotBlank String channelDistributionId,
        @NotBlank String journeyDistributionId,
        @NotNull Map<String, Object> answers
) {
    public ValidateQuestionnaireAnswersCommand toCommand() {
        return new ValidateQuestionnaireAnswersCommand(
                questionnaireId,
                channelDistributionId,
                journeyDistributionId,
                answers == null ? Map.of() : Map.copyOf(answers)
        );
    }
}

