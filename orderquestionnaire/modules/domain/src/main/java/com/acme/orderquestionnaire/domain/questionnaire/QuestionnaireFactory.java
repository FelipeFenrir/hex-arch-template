package com.acme.orderquestionnaire.domain.questionnaire;

import com.acme.orderquestionnaire.domain.questionnaire.errors.QuestionnaireDomainErrors;
import com.acme.orderquestionnaire.domain.questionnaire.validation.QuestionnaireBuilderRules;
import com.acme.orderquestionnaire.domain.questionnaire.validation.QuestionnaireIdentityRules;
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

    public static Result<Void, List<DomainError>> validateQuestionnaireId(String id) {
        List<DomainError> errors = DomainRuleRunner.validate(id, QuestionnaireIdentityRules.questionnaireIdRules());
        return errors.isEmpty() ? Result.success(null) : Result.failure(errors);
    }

    public static Result<Void, List<DomainError>> validateIdentityPayload(
            String id,
            String channelDistributionId,
            String journeyDistributionId
    ) {
        List<DomainError> errors = new ArrayList<>();
        collectIdentityErrors(errors, id, channelDistributionId, journeyDistributionId);
        return errors.isEmpty() ? Result.success(null) : Result.failure(errors);
    }

    public static Result<Void, List<DomainError>> validateCreatePayload(
            String id,
            String channelDistributionId,
            String journeyDistributionId,
            String description
    ) {
        List<DomainError> errors = new ArrayList<>();
        collectIdentityErrors(errors, id, channelDistributionId, journeyDistributionId);
        errors.addAll(DomainRuleRunner.validate(
                description,
                List.of(QuestionnaireIdentityRules.questionnaireDescriptionNotNull())
        ));
        return errors.isEmpty() ? Result.success(null) : Result.failure(errors);
    }

    public static Result<Void, List<DomainError>> validateUpdatePayload(
            String id,
            String channelDistributionId,
            String journeyDistributionId,
            String description
    ) {
        List<DomainError> errors = new ArrayList<>();
        collectIdentityErrors(errors, id, channelDistributionId, journeyDistributionId);
        if (description != null) {
            errors.addAll(DomainRuleRunner.validate(
                    description,
                    List.of(QuestionnaireIdentityRules.questionnaireDescriptionNotNull())
            ));
        }
        return errors.isEmpty() ? Result.success(null) : Result.failure(errors);
    }


    // -------- Questionnaire Builders --------

    public static Result<QuestionnaireBuilder, List<DomainError>> createNew(
            String id,
            String channelDistributionId,
            String journeyDistributionId,
            String description,
            AuditInfo auditInfo) {
        List<DomainError> errors = new ArrayList<>();
        collectIdentityErrors(errors, id, channelDistributionId, journeyDistributionId);
        errors.addAll(DomainRuleRunner.validate(description, List.of(QuestionnaireIdentityRules.questionnaireDescriptionNotNull())));
        errors.addAll(DomainRuleRunner.validate(auditInfo, QuestionnaireBuilderRules.auditInfoRules()));

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
        collectIdentityErrors(errors, id, channelDistributionId, journeyDistributionId);
        errors.addAll(DomainRuleRunner.validate(description, List.of(QuestionnaireIdentityRules.questionnaireDescriptionNotNull())));
        if (status == null) {
            errors.add(QuestionnaireDomainErrors.requiredObject(STATUS));
        }
        errors.addAll(DomainRuleRunner.validate(auditInfo, QuestionnaireBuilderRules.auditInfoRules()));

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

    // -------- Private Helpers --------

    private static void collectIdentityErrors(List<DomainError> errors,
                                              String id,
                                              String channelDistributionId,
                                              String journeyDistributionId) {
        errors.addAll(DomainRuleRunner.validate(id, QuestionnaireIdentityRules.questionnaireIdRules()));
        errors.addAll(DomainRuleRunner.validate(
                channelDistributionId,
                List.of(QuestionnaireIdentityRules.questionnaireChannelNotNull())
        ));
        errors.addAll(DomainRuleRunner.validate(
                journeyDistributionId,
                List.of(QuestionnaireIdentityRules.questionnaireJourneyNotNull())
        ));
    }

}

