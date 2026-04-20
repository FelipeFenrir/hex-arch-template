package com.acme.orderquestionnaire.domain.questionnaire;

import com.acme.orderquestionnaire.domain.questionnaire.errors.QuestionnaireDomainErrors;
import com.acme.orderquestionnaire.domain.questionnaire.validation.QuestionnaireBuilderRules;
import com.acme.orderquestionnaire.domain.questionnaire.validation.QuestionnaireIdentityRules;
import com.acme.orderquestionnaire.domain.question.answer.AnswerOptionItem;
import com.acme.orderquestionnaire.domain.questionnaire.conditioner.CompositeCondition;
import com.acme.orderquestionnaire.domain.questionnaire.conditioner.QuestionCondition;
import com.acme.orderquestionnaire.domain.questionnaire.vo.QuestionnaireId;
import com.acme.shared.enumerator.ParameterizationStatus;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;
import com.acme.shared.pattern.result.validation.DomainRuleRunner;
import com.acme.shared.vo.AuditInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class QuestionnaireFactory {

    public static final String STATUS = "status";

    private QuestionnaireFactory() {
        throw new IllegalStateException("Utility class");
    }


    // -------- Questionnaire Builders --------

    public static Result<QuestionnaireBuilder, List<DomainError>> createNew(
            String id,
            String channelDistributionId,
            String journeyDistributionId,
            String description,
            AuditInfo auditInfo) {
        List<DomainError> errors = new ArrayList<>();
        errors.addAll(DomainRuleRunner.validate(id, QuestionnaireIdentityRules.questionnaireIdRules()));
        errors.addAll(DomainRuleRunner.validate(channelDistributionId, List.of(QuestionnaireIdentityRules.questionnaireChannelNotNull())));
        errors.addAll(DomainRuleRunner.validate(journeyDistributionId, List.of(QuestionnaireIdentityRules.questionnaireJourneyNotNull())));
        errors.addAll(DomainRuleRunner.validate(description, List.of(QuestionnaireIdentityRules.questionnaireDescriptionNotNull())));
        errors.addAll(DomainRuleRunner.validate(auditInfo, QuestionnaireBuilderRules.auditInfoRules(auditInfo)));

        return errors.isEmpty()
                ? Result.success(new QuestionnaireBuilder(
                        QuestionnaireId.of(id, channelDistributionId, journeyDistributionId),
                        description,
                        auditInfo))
                : Result.failure(errors);
    }

    public static Result<QuestionnaireBuilder, List<DomainError>> rehydrate(
            String id,
            String channelDistributionId,
            String journeyDistributionId,
            String description,
            ParameterizationStatus status,
            AuditInfo auditInfo) {
        List<DomainError> errors = new ArrayList<>();
        errors.addAll(DomainRuleRunner.validate(id, QuestionnaireIdentityRules.questionnaireIdRules()));
        errors.addAll(DomainRuleRunner.validate(channelDistributionId, List.of(QuestionnaireIdentityRules.questionnaireChannelNotNull())));
        errors.addAll(DomainRuleRunner.validate(journeyDistributionId, List.of(QuestionnaireIdentityRules.questionnaireJourneyNotNull())));
        errors.addAll(DomainRuleRunner.validate(description, List.of(QuestionnaireIdentityRules.questionnaireDescriptionNotNull())));
        if (status == null) {
            errors.add(QuestionnaireDomainErrors.requiredObject(STATUS));
        }
        errors.addAll(DomainRuleRunner.validate(auditInfo, QuestionnaireBuilderRules.auditInfoRules(auditInfo)));

        return errors.isEmpty()
                ? Result.success(new QuestionnaireBuilder(
                        QuestionnaireId.of(id, channelDistributionId, journeyDistributionId),
                        description,
                        status,
                        auditInfo))
                : Result.failure(errors);
    }

    // -------- Questionnaire Builder --------

    public static final class QuestionnaireBuilder {
        public static final String QUESTION_RESULT = "questionResult";
        public static final String QUESTION = "question";
        public static final String QUESTIONS_LIST = "questionsList";
        public static final String QUESTION_ARRAY = "questionArray";
        public static final String STATUS_MUST_NOT_BE_NULL = "status must not be null";
        private final QuestionnaireId questionnaireId;
        private final String description;
        private final List<ConfiguredQuestion> questions;
        private final List<DomainError> errors;
        private final AuditInfo auditInfo;
        private final ParameterizationStatus status;

        private QuestionnaireBuilder(QuestionnaireId questionnaireId, String description, AuditInfo auditInfo) {
            this.questionnaireId = questionnaireId;
            this.description = description;
            this.auditInfo = auditInfo;
            this.status = null;
            this.questions = new ArrayList<>();
            this.errors = new ArrayList<>();
        }

        private QuestionnaireBuilder(QuestionnaireId questionnaireId,
                                     String description,
                                     ParameterizationStatus status,
                                     AuditInfo auditInfo) {
            this.questionnaireId = questionnaireId;
            this.description = description;
            this.auditInfo = auditInfo;
            this.status = Objects.requireNonNull(status, STATUS_MUST_NOT_BE_NULL);
            this.questions = new ArrayList<>();
            this.errors = new ArrayList<>();
        }

        public QuestionnaireBuilder withQuestion(ConfiguredQuestion question) {
            if (question == null) {
                this.errors.add(QuestionnaireDomainErrors.requiredObject(QUESTION));
            } else {
                this.questions.add(question);
            }
            return this;
        }

        public QuestionnaireBuilder withQuestion(Result<ConfiguredQuestion, List<DomainError>> questionResult) {
            switch (questionResult) {
                case null -> {
                    this.errors.add(QuestionnaireDomainErrors.requiredObject(QUESTION_RESULT));
                    return this;
                }
                case Result.Success<ConfiguredQuestion, List<DomainError>>(
                        ConfiguredQuestion value) -> this.questions.add(value);
                case Result.Failure<ConfiguredQuestion, List<DomainError>>(
                        List<DomainError> error) -> this.errors.addAll(error);
            }
            return this;
        }

        public QuestionnaireBuilder withQuestions(List<ConfiguredQuestion> questionsList) {
            if (questionsList == null) {
                this.errors.add(QuestionnaireDomainErrors.requiredObject(QUESTIONS_LIST));
            } else {
                this.questions.addAll(questionsList);
            }
            return this;
        }

        public QuestionnaireBuilder withQuestions(ConfiguredQuestion... questionArray) {
            if (questionArray == null) {
                this.errors.add(QuestionnaireDomainErrors.requiredObject(QUESTION_ARRAY));
            } else {
                this.questions.addAll(List.of(questionArray));
            }
            return this;
        }

        public Result<Questionnaire, List<DomainError>> build() {
            if (!errors.isEmpty()) {
                return Result.failure(List.copyOf(errors));
            }
            if (status == null) {
                Questionnaire questionnaire = Questionnaire.createNew(questionnaireId, description, auditInfo);
                questionnaire.addQuestions(questions);
                return Result.success(questionnaire);
            }
            return Result.success(Questionnaire.rehydrate(questionnaireId, description, status, questions, auditInfo));
        }
    }

    // -------- Helper Methods for Answer Options --------

    /**
     * Create a single answer option with value and label.
     */
    public static AnswerOptionItem option(String value, String label) {
        return AnswerOptionItem.createNew(value, label);
    }

    /**
     * Create a ListConfig with options (no custom error message).
     */
    public static ConfiguredQuestionFactory.ListConfig options(AnswerOptionItem... options) {
        return new ConfiguredQuestionFactory.ListConfig(List.of(options), null);
    }

    /**
     * Create a ListConfig with options and custom error message.
     */
    public static ConfiguredQuestionFactory.ListConfig options(String customErrorMessage, AnswerOptionItem... options) {
        return new ConfiguredQuestionFactory.ListConfig(List.of(options), customErrorMessage);
    }

    // -------- Condition Composition Helpers --------

    /**
     * Entry point for fluent condition composition using AND/OR with equal importance.
     * Allows building complex conditions without varargs.
     * Example:
     * <pre>
     * var condition = QuestionnaireFactory
     *     .condition(condition1)
     *     .and(condition2)
     *     .or(condition3)
     *     .and(condition4)
     *     .build();
     * </pre>
     */
    public static ConditionComposer condition(QuestionCondition first) {
        return new ConditionComposer(first);
    }

    /**
     * Fluent builder for composing conditions with AND/OR operators in equal priority.
     */
    public static final class ConditionComposer {
        public static final String FIRST_CONDITION_MUST_NOT_BE_NULL = "first condition must not be null";
        public static final String CONDITION_MUST_NOT_BE_NULL = "condition must not be null";
        private QuestionCondition current;

        private ConditionComposer(QuestionCondition first) {
            this.current = wrapCondition(Objects.requireNonNull(first, FIRST_CONDITION_MUST_NOT_BE_NULL));
        }

        /**
         * Add a condition with AND operator.
         */
        public ConditionComposer and(QuestionCondition other) {
            Objects.requireNonNull(other, CONDITION_MUST_NOT_BE_NULL);
            this.current = merge(this.current, other, true);
            return this;
        }

        /**
         * Add a condition with OR operator.
         */
        public ConditionComposer or(QuestionCondition other) {
            Objects.requireNonNull(other, CONDITION_MUST_NOT_BE_NULL);
            this.current = merge(this.current, other, false);
            return this;
        }

        /**
         * Build and return the final composed condition.
         */
        public QuestionCondition build() {
            return this.current;
        }
    }

    // -------- Shortcut Helpers for Common Cases --------

    /**
     * Shortcut helper to compose conditions with AND operator (varargs style).
     * For complex compositions, prefer ConditionComposer.condition(...).and(...).or(...).build()
     */
    public static QuestionCondition composeWithAnd(QuestionCondition... conditions) {
        return combineConditions(true, conditions);
    }

    /**
     * Shortcut helper to compose conditions with OR operator (varargs style).
     * For complex compositions, prefer ConditionComposer.condition(...).and(...).or(...).build()
     */
    public static QuestionCondition composeWithOr(QuestionCondition... conditions) {
        return combineConditions(false, conditions);
    }

    /**
     * Internal helper to combine conditions with a given operator.
     */
    public static QuestionCondition combineConditions(boolean isAnd, QuestionCondition... conditions) {
        if (conditions == null || conditions.length == 0) {
            return null;
        }
        if (conditions.length == 1) {
            return wrapCondition(conditions[0]);
        }

        QuestionCondition result = wrapCondition(conditions[0]);
        for (int i = 1; i < conditions.length; i++) {
            result = merge(result, conditions[i], isAnd);
        }
        return result;
    }

    // -------- Private Helpers --------

    private static QuestionCondition merge(QuestionCondition left, QuestionCondition right, boolean isAnd) {
        CompositeCondition composite = new CompositeCondition(isAnd);
        composite.addCondition(left);
        composite.addCondition(right);
        return composite;
    }

    private static QuestionCondition wrapCondition(QuestionCondition condition) {
        if (condition instanceof CompositeCondition) {
            return condition;
        }
        CompositeCondition composite = new CompositeCondition(true);
        composite.addCondition(condition);
        return composite;
    }
}

