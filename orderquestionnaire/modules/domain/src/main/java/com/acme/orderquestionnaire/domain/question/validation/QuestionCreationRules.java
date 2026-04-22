package com.acme.orderquestionnaire.domain.question.validation;

import com.acme.orderquestionnaire.domain.question.errors.QuestionDomainErrors;
import com.acme.shared.pattern.result.validation.DomainRule;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.vo.AuditInfo;
import java.util.List;

public final class QuestionCreationRules {

    private static final String SNAKE_CASE_PATTERN = "^[a-z]+(?:_[a-z0-9]+)*$";

    private QuestionCreationRules() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Regra: id deve estar presente (não nulo, não blank).
     */
    public static DomainRule<String> questionIdNotNull() {
        return new DomainRule<>() {
            @Override
            public boolean isSatisfiedBy(String candidate) {
                return candidate != null && !candidate.isBlank();
            }

            @Override
            public DomainError toDomainError() {
                return QuestionDomainErrors.requiredField("id");
            }
        };
    }

    /**
     * Regra: id deve estar em formato snake_case.
     */
    public static DomainRule<String> questionIdSnakeCaseFormat() {
        return new DomainRule<>() {
            @Override
            public boolean isSatisfiedBy(String candidate) {
                return candidate == null || candidate.isBlank() || candidate.matches(SNAKE_CASE_PATTERN);
            }

            @Override
            public DomainError toDomainError() {
                return QuestionDomainErrors.invalidIdFormat();
            }
        };
    }

    /**
     * Regra: label deve estar presente (não nulo, não blank).
     */
    public static DomainRule<String> questionLabelNotNull() {
        return new DomainRule<>() {
            @Override
            public boolean isSatisfiedBy(String candidate) {
                return candidate != null && !candidate.isBlank();
            }

            @Override
            public DomainError toDomainError() {
                return QuestionDomainErrors.requiredField("label");
            }
        };
    }

    /**
     * Factory: retorna lista de regras para criar Question.
     */
    public static List<DomainRule<String>> createNewQuestionIdRules() {
        return List.of(
                questionIdNotNull(),
                questionIdSnakeCaseFormat()
        );
    }

    public static List<DomainRule<String>> createNewQuestionLabelRules() {
        return List.of(
                questionLabelNotNull()
        );
    }

    /**
     * Regra: auditInfo deve estar presente (não nulo).
     */
    public static DomainRule<AuditInfo> auditInfoNotNull() {
        return new DomainRule<>() {
            @Override
            public boolean isSatisfiedBy(AuditInfo candidate) {
                return candidate != null;
            }

            @Override
            public DomainError toDomainError() {
                return QuestionDomainErrors.requiredObject("auditInfo");
            }
        };
    }

    /**
     * Factory: retorna lista de regras para validar auditInfo na criação de Question.
     */
    public static List<DomainRule<AuditInfo>> createNewAuditInfoRules() {
        return List.of(
                auditInfoNotNull()
        );
    }
}
