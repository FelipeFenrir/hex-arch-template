package com.acme.orderquestionnaire.application.questionnaire.service.support;

import com.acme.orderquestionnaire.application.question.error.QuestionErrors;
import com.acme.orderquestionnaire.application.questionnaire.dto.command.AnswerConfigParam;
import com.acme.orderquestionnaire.application.questionnaire.dto.command.ConfiguredQuestionParam;
import com.acme.orderquestionnaire.application.questionnaire.service.support.condition.ConditionAssembler;
import com.acme.orderquestionnaire.domain.question.Question;
import com.acme.orderquestionnaire.domain.questionnaire.ConfiguredQuestion;
import com.acme.orderquestionnaire.domain.questionnaire.ConfiguredQuestionFactory;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;

import java.util.List;

public final class ConfiguredQuestionAssembler {

    private ConfiguredQuestionAssembler() {
        throw new IllegalStateException("Utility class");
    }

    public static Result<ConfiguredQuestion, List<DomainError>> build(
            Question question,
            ConfiguredQuestionParam param) {
        if (param == null || param.answerConfiguration() == null) {
            return QuestionErrors.INVALID_COMMAND.asFailure();
        }

        return ConfiguredQuestionFactory.from(question)
                .flatMap(builder -> ConditionAssembler.toDomain(param.rootCondition())
                        .flatMap(rootCondition -> {
                    ConfiguredQuestionFactory.ConfiguredQuestionBuilder configuredBuilder = builder
                            .withOrder(param.order())
                            .withCondition(rootCondition);
                    return switch (param.answerConfiguration()) {
                        case AnswerConfigParam.Text text -> configuredBuilder.asText(
                                new ConfiguredQuestionFactory.TextConfig(text.regexPattern(), text.customErrorMessage())
                        );
                        case AnswerConfigParam.Number number -> configuredBuilder.asNumber(
                                new ConfiguredQuestionFactory.NumberConfig(
                                        number.min(),
                                        number.max(),
                                        number.step(),
                                        number.allowedDecimal(),
                                        number.allowedNegative(),
                                        number.customErrorMessage()
                                )
                        );
                        case AnswerConfigParam.Date date -> configuredBuilder.asDate(
                                new ConfiguredQuestionFactory.DateConfig(date.maskFormat(), date.allowPastDates(), date.customErrorMessage())
                        );
                        case AnswerConfigParam.OptionList optionList -> configuredBuilder.asOptionList(
                                new ConfiguredQuestionFactory.ListConfig(optionList.options(), optionList.customErrorMessage())
                        );
                    };
                }));
    }
}