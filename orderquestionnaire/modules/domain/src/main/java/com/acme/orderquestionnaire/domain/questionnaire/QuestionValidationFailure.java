package com.acme.orderquestionnaire.domain.questionnaire;

import com.acme.shared.pattern.result.DomainError;

import java.util.List;

public record QuestionValidationFailure(String questionId,
                                        String questionLabel,
                                        int order,
                                        List<DomainError> errors) {

    public QuestionValidationFailure {
        errors = errors == null ? List.of() : List.copyOf(errors);
    }
}

