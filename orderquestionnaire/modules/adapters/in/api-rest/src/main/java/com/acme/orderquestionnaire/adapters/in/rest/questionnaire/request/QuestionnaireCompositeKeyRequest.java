package com.acme.orderquestionnaire.adapters.in.rest.questionnaire.request;

import jakarta.validation.constraints.NotBlank;

public record QuestionnaireCompositeKeyRequest(
        @NotBlank String channelId,
        @NotBlank String journeyId
) {
}

