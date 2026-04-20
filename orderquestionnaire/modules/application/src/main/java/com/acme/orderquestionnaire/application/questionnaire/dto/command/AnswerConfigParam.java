package com.acme.orderquestionnaire.application.questionnaire.dto.command;

import com.acme.orderquestionnaire.domain.question.answer.AnswerOptionItem;

import java.util.List;

public sealed interface AnswerConfigParam permits
        AnswerConfigParam.Text,
        AnswerConfigParam.Number,
        AnswerConfigParam.Date,
        AnswerConfigParam.OptionList {

    record Text(String regexPattern, String customErrorMessage) implements AnswerConfigParam { }

    record Number(Double min,
                  Double max,
                  Double step,
                  boolean allowedDecimal,
                  boolean allowedNegative,
                  String customErrorMessage) implements AnswerConfigParam { }

    record Date(String maskFormat, boolean allowPastDates, String customErrorMessage)
            implements AnswerConfigParam { }

    record OptionList(List<AnswerOptionItem> options, String customErrorMessage)
            implements AnswerConfigParam { }
}

