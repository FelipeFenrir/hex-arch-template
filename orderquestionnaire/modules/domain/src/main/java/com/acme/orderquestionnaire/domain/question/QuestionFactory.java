package com.acme.orderquestionnaire.domain.question;

import com.acme.orderquestionnaire.domain.question.validation.QuestionBuilderRules;
import com.acme.orderquestionnaire.domain.question.validation.QuestionCreationRules;
import com.acme.orderquestionnaire.domain.question.validation.QuestionRehydrationRules;
import com.acme.shared.enumerator.ParameterizationStatus;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;
import com.acme.shared.pattern.result.validation.DomainRuleRunner;
import com.acme.shared.vo.AuditInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class QuestionFactory {

    private QuestionFactory() {
        throw new IllegalStateException("Utility class");
    }

    public static Result<Void, List<DomainError>> validateQuestionId(String id) {
        List<DomainError> errors = DomainRuleRunner.validate(id, QuestionCreationRules.createNewQuestionIdRules(id));
        return errors.isEmpty() ? Result.success(null) : Result.failure(errors);
    }

    public static Result<Void, List<DomainError>> validateCreatePayload(
            String id,
            String label,
            String salesItemReferenceCode
    ) {
        List<DomainError> errors = new ArrayList<>();
        errors.addAll(DomainRuleRunner.validate(id, QuestionCreationRules.createNewQuestionIdRules(id)));
        errors.addAll(DomainRuleRunner.validate(label, QuestionCreationRules.createNewQuestionLabelRules(label)));
        errors.addAll(DomainRuleRunner.validate(
                salesItemReferenceCode,
                QuestionBuilderRules.salesItemReferenceCodeRules(salesItemReferenceCode)
        ));
        return errors.isEmpty() ? Result.success(null) : Result.failure(errors);
    }

    public static Result<Void, List<DomainError>> validateUpdatePayload(
            String id,
            String label,
            String salesItemReferenceCode
    ) {
        List<DomainError> errors = new ArrayList<>();
        errors.addAll(DomainRuleRunner.validate(id, QuestionRehydrationRules.rehydrateQuestionIdRules(id)));
        errors.addAll(DomainRuleRunner.validate(label, QuestionRehydrationRules.rehydrateQuestionLabelRules(label)));
        errors.addAll(DomainRuleRunner.validate(
                salesItemReferenceCode,
                QuestionBuilderRules.salesItemReferenceCodeRules(salesItemReferenceCode)
        ));
        return errors.isEmpty() ? Result.success(null) : Result.failure(errors);
    }

    public static Result<NewQuestionBuilder, List<DomainError>> createNew(String id, String label, AuditInfo auditInfo) {
        List<DomainError> errors = new ArrayList<>();
        errors.addAll(DomainRuleRunner.validate(id, QuestionCreationRules.createNewQuestionIdRules(id)));
        errors.addAll(DomainRuleRunner.validate(label, QuestionCreationRules.createNewQuestionLabelRules(label)));
        errors.addAll(DomainRuleRunner.validate(auditInfo, QuestionCreationRules.createNewAuditInfoRules(auditInfo)));

        return errors.isEmpty()
                ? Result.success(new NewQuestionBuilder(id, label, auditInfo))
                : Result.failure(errors);
    }

    public static Result<RehydratedQuestionBuilder, List<DomainError>> rehydrate(
            String id,
            String label,
            ParameterizationStatus status,
            AuditInfo auditInfo) {
        List<DomainError> errors = new ArrayList<>();
        errors.addAll(DomainRuleRunner.validate(id, QuestionRehydrationRules.rehydrateQuestionIdRules(id)));
        errors.addAll(DomainRuleRunner.validate(label, QuestionRehydrationRules.rehydrateQuestionLabelRules(label)));
        errors.addAll(DomainRuleRunner.validate(status, QuestionRehydrationRules.rehydrateQuestionStatusRules(status)));
        errors.addAll(DomainRuleRunner.validate(auditInfo, QuestionRehydrationRules.rehydrateAuditInfoRules(auditInfo)));

        return errors.isEmpty()
                ? Result.success(new RehydratedQuestionBuilder(id, label, status, auditInfo))
                : Result.failure(errors);
    }

    public abstract static class AbstractBuilder<T extends AbstractBuilder<T>> {

        protected final String id;
        protected final String label;
        protected String salesItemReferenceCode;
        protected final AuditInfo auditInfo;
        protected final List<DomainError> errors;

        protected AbstractBuilder(String id, String label, AuditInfo auditInfo) {
            this.id = id;
            this.label = label;
            this.auditInfo = auditInfo;
            this.errors = new ArrayList<>();
        }

        public T withSalesItemReferenceCode(String salesItemReferenceCode) {
            var validationErrors = DomainRuleRunner.validate(
                    salesItemReferenceCode,
                    QuestionBuilderRules.salesItemReferenceCodeRules(salesItemReferenceCode));
            this.errors.addAll(validationErrors);

            if (!validationErrors.isEmpty()) {
                return self();
            }
            this.salesItemReferenceCode = salesItemReferenceCode;
            return self();
        }

        protected abstract T self();

        public abstract Result<Question, List<DomainError>> build();
    }

    public static final class NewQuestionBuilder extends AbstractBuilder<NewQuestionBuilder> {
        private NewQuestionBuilder(String id, String label, AuditInfo auditInfo) {
            super(id, label, auditInfo);
        }

        @Override
        protected NewQuestionBuilder self() {
            return this;
        }

        @Override
        public Result<Question, List<DomainError>> build() {
            if (!errors.isEmpty()) {
                return Result.failure(List.copyOf(errors));
            }
            return Result.success(Question.createNew(id, label, salesItemReferenceCode, auditInfo));
        }
    }

    public static final class RehydratedQuestionBuilder extends AbstractBuilder<RehydratedQuestionBuilder> {
        public static final String STATUS_MUST_NOT_BE_NULL = "status must not be null";

        private final ParameterizationStatus status;

        private RehydratedQuestionBuilder(String id, String label, ParameterizationStatus status, AuditInfo auditInfo) {
            super(id, label, auditInfo);
            this.status = Objects.requireNonNull(status, STATUS_MUST_NOT_BE_NULL);
        }

        @Override
        protected RehydratedQuestionBuilder self() {
            return this;
        }

        @Override
        public Result<Question, List<DomainError>> build() {
            if (!errors.isEmpty()) {
                return Result.failure(List.copyOf(errors));
            }
            return Result.success(Question.rehydrate(id, label, status, salesItemReferenceCode, auditInfo));
        }
    }
}
