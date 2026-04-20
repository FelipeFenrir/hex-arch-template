package com.acme.orderquestionnaire.domain.audit;

import com.acme.orderquestionnaire.domain.audit.validation.AuditInfoValidationRules;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;
import com.acme.shared.pattern.result.validation.DomainRuleRunner;
import com.acme.shared.vo.AuditInfo;
import com.acme.shared.vo.AuditUser;
import com.acme.shared.vo.Id;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class OrderQuestionnaireAuditFactory {

    private OrderQuestionnaireAuditFactory() {
        throw new IllegalStateException("Utility class");
    }

    public static Result<AuditUser, List<DomainError>> user(Id id, String referenceCode, String name, String email) {
        return userForQuestionnaire(id, referenceCode, name, email);
    }

    public static Result<AuditUser, List<DomainError>> userForQuestionnaire(
            Id id,
            String referenceCode,
            String name,
            String email
    ) {
        return validateUser(id, name,
                AuditInfoValidationRules.questionnaireUserIdRules(),
                AuditInfoValidationRules.questionnaireUserNameRules())
                .map(__ -> new OrderQuestionnaireAuditUser(id, referenceCode, name, email));
    }

    public static Result<AuditUser, List<DomainError>> userForQuestion(
            Id id,
            String referenceCode,
            String name,
            String email
    ) {
        return validateUser(id, name,
                AuditInfoValidationRules.questionUserIdRules(),
                AuditInfoValidationRules.questionUserNameRules())
                .map(__ -> new OrderQuestionnaireAuditUser(id, referenceCode, name, email));
    }

    public static Result<AuditInfo, List<DomainError>> createNew(
            Id id,
            String referenceCode,
            String name,
            String email,
            LocalDateTime createdAt
    ) {
        return createNewForQuestionnaire(id, referenceCode, name, email, createdAt);
    }

    public static Result<AuditInfo, List<DomainError>> createNewForQuestionnaire(
            Id id,
            String referenceCode,
            String name,
            String email,
            LocalDateTime createdAt
    ) {
        return buildCreateAudit(
                id,
                referenceCode,
                name,
                email,
                createdAt,
                AuditInfoValidationRules.questionnaireUserIdRules(),
                AuditInfoValidationRules.questionnaireUserNameRules(),
                AuditInfoValidationRules.questionnaireCreatedAtRules()
        );
    }

    public static Result<AuditInfo, List<DomainError>> createNewForQuestion(
            Id id,
            String referenceCode,
            String name,
            String email,
            LocalDateTime createdAt
    ) {
        return buildCreateAudit(
                id,
                referenceCode,
                name,
                email,
                createdAt,
                AuditInfoValidationRules.questionUserIdRules(),
                AuditInfoValidationRules.questionUserNameRules(),
                AuditInfoValidationRules.questionCreatedAtRules()
        );
    }

    public static Result<AuditUser, List<DomainError>> updateUserForQuestionnaire(
            Id id,
            String referenceCode,
            String name,
            String email,
            LocalDateTime updatedAt
    ) {
        List<DomainError> errors = new ArrayList<>();
        errors.addAll(DomainRuleRunner.validate(updatedAt, AuditInfoValidationRules.questionnaireUpdatedAtRules()));
        Result<Void, List<DomainError>> userValidation = validateUser(
                id,
                name,
                AuditInfoValidationRules.questionnaireUserIdRules(),
                AuditInfoValidationRules.questionnaireUserNameRules());
        if (userValidation.isFailure()) {
            errors.addAll(userValidation.errorOrElseThrow(() -> new IllegalStateException("Expected failure result")));
        }
        return errors.isEmpty()
                ? Result.success(new OrderQuestionnaireAuditUser(id, referenceCode, name, email))
                : Result.failure(errors);
    }

    public static Result<AuditUser, List<DomainError>> updateUserForQuestion(
            Id id,
            String referenceCode,
            String name,
            String email,
            LocalDateTime updatedAt
    ) {
        List<DomainError> errors = new ArrayList<>();
        errors.addAll(DomainRuleRunner.validate(updatedAt, AuditInfoValidationRules.questionUpdatedAtRules()));
        Result<Void, List<DomainError>> userValidation = validateUser(
                id,
                name,
                AuditInfoValidationRules.questionUserIdRules(),
                AuditInfoValidationRules.questionUserNameRules());
        if (userValidation.isFailure()) {
            errors.addAll(userValidation.errorOrElseThrow(() -> new IllegalStateException("Expected failure result")));
        }
        return errors.isEmpty()
                ? Result.success(new OrderQuestionnaireAuditUser(id, referenceCode, name, email))
                : Result.failure(errors);
    }

    private static Result<AuditInfo, List<DomainError>> buildCreateAudit(
            Id id,
            String referenceCode,
            String name,
            String email,
            LocalDateTime createdAt,
            List<com.acme.shared.pattern.result.validation.DomainRule<Id>> idRules,
            List<com.acme.shared.pattern.result.validation.DomainRule<String>> nameRules,
            List<com.acme.shared.pattern.result.validation.DomainRule<LocalDateTime>> createdAtRules
    ) {
        List<DomainError> errors = new ArrayList<>();
        errors.addAll(DomainRuleRunner.validate(id, idRules));
        errors.addAll(DomainRuleRunner.validate(name, nameRules));
        errors.addAll(DomainRuleRunner.validate(createdAt, createdAtRules));
        if (!errors.isEmpty()) {
            return Result.failure(errors);
        }
        AuditUser createdBy = new OrderQuestionnaireAuditUser(id, referenceCode, name, email);
        return Result.success(new OrderQuestionnaireAuditInfo(createdBy, createdAt, null, null));
    }

    private static Result<Void, List<DomainError>> validateUser(
            Id id,
            String name,
            List<com.acme.shared.pattern.result.validation.DomainRule<Id>> idRules,
            List<com.acme.shared.pattern.result.validation.DomainRule<String>> nameRules
    ) {
        List<DomainError> errors = new ArrayList<>();
        errors.addAll(DomainRuleRunner.validate(id, idRules));
        errors.addAll(DomainRuleRunner.validate(name, nameRules));
        return errors.isEmpty() ? Result.success(null) : Result.failure(errors);
    }
}

