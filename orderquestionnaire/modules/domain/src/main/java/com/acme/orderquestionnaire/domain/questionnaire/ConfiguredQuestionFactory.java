package com.acme.orderquestionnaire.domain.questionnaire;

import com.acme.orderquestionnaire.domain.questionnaire.errors.QuestionnaireDomainErrors;
import com.acme.orderquestionnaire.domain.question.Question;
import com.acme.orderquestionnaire.domain.question.answer.AnswerConfiguration;
import com.acme.orderquestionnaire.domain.question.answer.AnswerOptionItem;
import com.acme.orderquestionnaire.domain.question.answer.strategy.AnswerConfigurationFactory;
import com.acme.orderquestionnaire.domain.questionnaire.conditioner.QuestionCondition;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class ConfiguredQuestionFactory {

    public static final String QUESTION = "question";

    private ConfiguredQuestionFactory() {
    }

    // Configuration records
    public record TextConfig(String regexPattern, String customErrorMessage) {}
    public record NumberConfig(
            Double min,
            Double max,
            Double step,
            boolean allowedDecimal,
            boolean allowedNegative,
            String customErrorMessage) {}
    public record DateConfig(String maskFormat, boolean allowPastDates, String customErrorMessage) {}
    public record ListConfig(List<AnswerOptionItem> options, String customErrorMessage) {}

    private static final TextConfig DEFAULT_TEXT_CONFIG = new TextConfig(null, null);
    private static final NumberConfig DEFAULT_NUMBER_CONFIG = new NumberConfig(null, null, null, true, true, null);
    private static final DateConfig DEFAULT_DATE_CONFIG = new DateConfig(null, true, null);

    /**
     * Entry point: create a fluent builder for configuring a Question.
     */
    public static Result<QuestionBuilder, List<DomainError>> from(Question question) {
        if (question == null) {
            return Result.failure(List.of(QuestionnaireDomainErrors.requiredObject(QUESTION)));
        }
        return Result.success(new QuestionBuilder(question));
    }

    // -------- Fluent builder --------

    public static final class QuestionBuilder {
        public static final String QUESTION_MUST_NOT_BE_NULL = "question must not be null";
        public static final String ANSWER_CONFIGURATION = "answerConfiguration";

        private final Question question;
        private int order = 0;
        private QuestionCondition condition;
        private final List<DomainError> errors = new ArrayList<>();

        private QuestionBuilder(Question question) {
            this.question = Objects.requireNonNull(question, QUESTION_MUST_NOT_BE_NULL);
        }

        public QuestionBuilder withOrder(Integer order) {
            if (order != null && order >= 0) {
                this.order = order;
            } else if (order != null) {
                this.errors.add(QuestionnaireDomainErrors.invalidOrder());
            }
            return this;
        }

        public QuestionBuilder withCondition(QuestionCondition condition) {
            this.condition = condition;
            return this;
        }

        public Result<ConfiguredQuestion, List<DomainError>> asText() {
            return asText(DEFAULT_TEXT_CONFIG);
        }

        public Result<ConfiguredQuestion, List<DomainError>> asText(TextConfig config) {
            TextConfig safeConfig = config == null ? DEFAULT_TEXT_CONFIG : config;
            AnswerConfiguration strategy = AnswerConfigurationFactory
                    .createTextStrategy(safeConfig.regexPattern(), safeConfig.customErrorMessage());
            return build(strategy);
        }

        public Result<ConfiguredQuestion, List<DomainError>> asNumber() {
            return asNumber(DEFAULT_NUMBER_CONFIG);
        }

        public Result<ConfiguredQuestion, List<DomainError>> asNumber(NumberConfig config) {
            NumberConfig safeConfig = config == null ? DEFAULT_NUMBER_CONFIG : config;
            AnswerConfiguration strategy = AnswerConfigurationFactory.createNumberStrategy(
                    safeConfig.min(),
                    safeConfig.max(),
                    safeConfig.step(),
                    safeConfig.allowedDecimal(),
                    safeConfig.allowedNegative(),
                    safeConfig.customErrorMessage()
            );
            return build(strategy);
        }

        public Result<ConfiguredQuestion, List<DomainError>> asDate() {
            return asDate(DEFAULT_DATE_CONFIG);
        }

        public Result<ConfiguredQuestion, List<DomainError>> asDate(DateConfig config) {
            DateConfig safeConfig = config == null ? DEFAULT_DATE_CONFIG : config;
            AnswerConfiguration strategy = AnswerConfigurationFactory.createDateStrategy(
                    safeConfig.maskFormat(),
                    safeConfig.allowPastDates(),
                    safeConfig.customErrorMessage()
            );
            return build(strategy);
        }

        public Result<ConfiguredQuestion, List<DomainError>> asOptionList(ListConfig config) {
            ListConfig safeConfig = config == null ? new ListConfig(List.of(), null) : config;
            return asOptionList(safeConfig.options(), safeConfig.customErrorMessage());
        }

        public Result<ConfiguredQuestion, List<DomainError>> asOptionList(List<AnswerOptionItem> options, String customErrorMessage) {
            AnswerConfiguration strategy = AnswerConfigurationFactory.createListStrategy(options, customErrorMessage);
            return build(strategy);
        }

        private Result<ConfiguredQuestion, List<DomainError>> build(AnswerConfiguration strategy) {
            if (strategy == null) {
                this.errors.add(QuestionnaireDomainErrors.requiredObject(ANSWER_CONFIGURATION));
            }
            if (!errors.isEmpty()) {
                return Result.failure(List.copyOf(errors));
            }

            ConfiguredQuestion configuredQuestion = ConfiguredQuestion.createNew(question, strategy, order);
            if (condition != null) {
                configuredQuestion.rootCondition(condition);
            }
            return Result.success(configuredQuestion);
        }
    }
}