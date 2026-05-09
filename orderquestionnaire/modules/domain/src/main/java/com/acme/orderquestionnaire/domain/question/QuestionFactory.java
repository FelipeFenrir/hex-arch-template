package com.acme.orderquestionnaire.domain.question;

import com.acme.orderquestionnaire.domain.question.validation.QuestionBuilderRules;
import com.acme.orderquestionnaire.domain.question.validation.QuestionCreationRules;
import com.acme.orderquestionnaire.domain.question.validation.QuestionRehydrationRules;
import com.acme.shared.enumerator.ParameterizationStatus;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;
import com.acme.shared.pattern.result.validation.DomainRuleRunner;
import com.acme.shared.vo.AuditInfo;
import com.acme.shared.vo.QuestionId;
import com.acme.shared.vo.SalesItemReferenceCode;

import java.util.ArrayList;
import java.util.List;

public final class QuestionFactory {

    private QuestionFactory() {
        throw new IllegalStateException("Utility class");
    }

    public static Result<Void, List<DomainError>> validateQuestionId(String id) {
        List<DomainError> errors = DomainRuleRunner.validate(id, QuestionCreationRules.createNewQuestionIdRules());
        return errors.isEmpty() ? Result.success(null) : Result.failure(errors);
    }

    public static Result<Void, List<DomainError>> validateCreatePayload(
            String id,
            String label,
            String salesItemReferenceCode
    ) {
        List<DomainError> errors = new ArrayList<>();
        errors.addAll(DomainRuleRunner.validate(id, QuestionCreationRules.createNewQuestionIdRules()));
        errors.addAll(DomainRuleRunner.validate(label, QuestionCreationRules.createNewQuestionLabelRules()));
        errors.addAll(DomainRuleRunner.validate(
                salesItemReferenceCode,
                QuestionBuilderRules.salesItemReferenceCodeRules()
        ));
        return errors.isEmpty() ? Result.success(null) : Result.failure(errors);
    }

    public static Result<Void, List<DomainError>> validateUpdatePayload(
            String id,
            String label,
            String salesItemReferenceCode
    ) {
        List<DomainError> errors = new ArrayList<>();
        errors.addAll(DomainRuleRunner.validate(id, QuestionRehydrationRules.rehydrateQuestionIdRules()));
        errors.addAll(DomainRuleRunner.validate(label, QuestionRehydrationRules.rehydrateQuestionLabelRules()));
        errors.addAll(DomainRuleRunner.validate(
                salesItemReferenceCode,
                QuestionBuilderRules.salesItemReferenceCodeRules()
        ));
        return errors.isEmpty() ? Result.success(null) : Result.failure(errors);
    }

    public static Result<NewQuestionBuilder, List<DomainError>> createNew(
            String id,
            String label,
            String salesItemReferenceCode,
            AuditInfo auditInfo) {
        List<DomainError> errors = collectCreateErrors(id, label, salesItemReferenceCode, auditInfo);

        return errors.isEmpty()
                ? Result.success(new NewQuestionBuilder()
                        .withId(id)
                        .withLabel(label)
                        .withSalesItemReferenceCode(salesItemReferenceCode)
                        .withAuditInfo(auditInfo))
                : Result.failure(errors);
    }

    public static Result<NewQuestionBuilder, List<DomainError>> createNew(
            QuestionId id,
            String label,
            SalesItemReferenceCode salesItemReferenceCode,
            AuditInfo auditInfo) {
        return createNew(
                id == null ? null : id.value(),
                label,
                salesItemReferenceCode == null ? null : salesItemReferenceCode.value(),
                auditInfo
        );
    }

    public static Result<RehydratedQuestionBuilder, List<DomainError>> rehydrate(
            String id,
            String label,
            ParameterizationStatus status,
            String salesItemReferenceCode,
            AuditInfo auditInfo) {
        List<DomainError> errors = collectRehydrationErrors(id, label, status, salesItemReferenceCode, auditInfo);

        return errors.isEmpty()
                ? Result.success(new RehydratedQuestionBuilder()
                        .withId(id)
                        .withLabel(label)
                        .withStatus(status)
                        .withSalesItemReferenceCode(salesItemReferenceCode)
                        .withAuditInfo(auditInfo))
                : Result.failure(errors);
    }

    public static Result<RehydratedQuestionBuilder, List<DomainError>> rehydrate(
            QuestionId id,
            String label,
            ParameterizationStatus status,
            SalesItemReferenceCode salesItemReferenceCode,
            AuditInfo auditInfo) {
        return rehydrate(
                id == null ? null : id.value(),
                label,
                status,
                salesItemReferenceCode == null ? null : salesItemReferenceCode.value(),
                auditInfo
        );
    }

    public static NewQuestionBuilder createNewBuilder() {
        return new NewQuestionBuilder();
    }

    public static RehydratedQuestionBuilder rehydratedBuilder() {
        return new RehydratedQuestionBuilder();
    }

    private static List<DomainError> collectCreateErrors(String id,
                                                         String label,
                                                         String salesItemReferenceCode,
                                                         AuditInfo auditInfo) {
        List<DomainError> errors = new ArrayList<>();
        errors.addAll(DomainRuleRunner.validate(id, QuestionCreationRules.createNewQuestionIdRules()));
        errors.addAll(DomainRuleRunner.validate(label, QuestionCreationRules.createNewQuestionLabelRules()));
        errors.addAll(DomainRuleRunner.validate(salesItemReferenceCode, QuestionBuilderRules.salesItemReferenceCodeRules()));
        errors.addAll(DomainRuleRunner.validate(auditInfo, QuestionCreationRules.createNewAuditInfoRules()));
        return errors;
    }

    private static List<DomainError> collectRehydrationErrors(String id,
                                                              String label,
                                                              ParameterizationStatus status,
                                                              String salesItemReferenceCode,
                                                              AuditInfo auditInfo) {
        List<DomainError> errors = new ArrayList<>();
        errors.addAll(DomainRuleRunner.validate(id, QuestionRehydrationRules.rehydrateQuestionIdRules()));
        errors.addAll(DomainRuleRunner.validate(label, QuestionRehydrationRules.rehydrateQuestionLabelRules()));
        errors.addAll(DomainRuleRunner.validate(status, QuestionRehydrationRules.rehydrateQuestionStatusRules()));
        errors.addAll(DomainRuleRunner.validate(salesItemReferenceCode, QuestionBuilderRules.salesItemReferenceCodeRules()));
        errors.addAll(DomainRuleRunner.validate(auditInfo, QuestionRehydrationRules.rehydrateAuditInfoRules()));
        return errors;
    }

    public static final class NewQuestionBuilder {
        private String id;
        private String label;
        private String salesItemReferenceCode;
        private AuditInfo auditInfo;
        private final List<DomainError> errors = new ArrayList<>();

        private NewQuestionBuilder() {
        }

        public NewQuestionBuilder withId(String id) {
            this.id = id;
            return this;
        }

        public NewQuestionBuilder withId(QuestionId id) {
            this.id = id == null ? null : id.value();
            return this;
        }

        public NewQuestionBuilder withLabel(String label) {
            this.label = label;
            return this;
        }

        public NewQuestionBuilder withSalesItemReferenceCode(String salesItemReferenceCode) {
            this.salesItemReferenceCode = salesItemReferenceCode;
            return this;
        }

        public NewQuestionBuilder withSalesItemReferenceCode(SalesItemReferenceCode salesItemReferenceCode) {
            this.salesItemReferenceCode = salesItemReferenceCode == null ? null : salesItemReferenceCode.value();
            return this;
        }

        public NewQuestionBuilder withAuditInfo(AuditInfo auditInfo) {
            this.auditInfo = auditInfo;
            return this;
        }

        public Result<Question, List<DomainError>> build() {
            errors.addAll(collectCreateErrors(id, label, salesItemReferenceCode, auditInfo));
            if (!errors.isEmpty()) {
                return Result.failure(List.copyOf(errors));
            }
            return Result.success(Question.createNew(QuestionId.of(id), label, SalesItemReferenceCode.of(salesItemReferenceCode), auditInfo));
        }
    }

    public static final class RehydratedQuestionBuilder {
        private String id;
        private String label;
        private ParameterizationStatus status;
        private String salesItemReferenceCode;
        private AuditInfo auditInfo;
        private final List<DomainError> errors = new ArrayList<>();

        private RehydratedQuestionBuilder() {
        }

        public RehydratedQuestionBuilder withId(String id) {
            this.id = id;
            return this;
        }

        public RehydratedQuestionBuilder withId(QuestionId id) {
            this.id = id == null ? null : id.value();
            return this;
        }

        public RehydratedQuestionBuilder withLabel(String label) {
            this.label = label;
            return this;
        }

        public RehydratedQuestionBuilder withStatus(ParameterizationStatus status) {
            this.status = status;
            return this;
        }

        public RehydratedQuestionBuilder withSalesItemReferenceCode(String salesItemReferenceCode) {
            this.salesItemReferenceCode = salesItemReferenceCode;
            return this;
        }

        public RehydratedQuestionBuilder withSalesItemReferenceCode(SalesItemReferenceCode salesItemReferenceCode) {
            this.salesItemReferenceCode = salesItemReferenceCode == null ? null : salesItemReferenceCode.value();
            return this;
        }

        public RehydratedQuestionBuilder withAuditInfo(AuditInfo auditInfo) {
            this.auditInfo = auditInfo;
            return this;
        }

        public Result<Question, List<DomainError>> build() {
            errors.addAll(collectRehydrationErrors(id, label, status, salesItemReferenceCode, auditInfo));
            if (!errors.isEmpty()) {
                return Result.failure(List.copyOf(errors));
            }
            return Result.success(Question.rehydrate(QuestionId.of(id), label, status, SalesItemReferenceCode.of(salesItemReferenceCode), auditInfo));
        }
    }
}
