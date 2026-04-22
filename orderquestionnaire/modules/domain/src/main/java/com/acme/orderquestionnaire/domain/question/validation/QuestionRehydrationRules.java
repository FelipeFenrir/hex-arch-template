package com.acme.orderquestionnaire.domain.question.validation;

import com.acme.orderquestionnaire.domain.question.errors.QuestionDomainErrors;
import com.acme.shared.pattern.result.validation.DomainRule;
import com.acme.shared.enumerator.ParameterizationStatus;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.vo.AuditInfo;
import java.util.List;

public final class QuestionRehydrationRules {

    private QuestionRehydrationRules() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Regra: id deve estar presente (não nulo, não blank).
     */
    public static DomainRule<String> questionIdNotNull() {
        return new DomainRule<String>() {
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
     * Regra: label deve estar presente (não nulo, não blank).
     */
    public static DomainRule<String> questionLabelNotNull() {
        return new DomainRule<String>() {
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
     * Regra: status deve estar presente (não nulo).
     */
    public static DomainRule<ParameterizationStatus> questionStatusNotNull() {
        return new DomainRule<ParameterizationStatus>() {
            @Override
            public boolean isSatisfiedBy(ParameterizationStatus candidate) {
                return candidate != null;
            }

            @Override
            public DomainError toDomainError() {
                return QuestionDomainErrors.requiredObject("status");
            }
        };
    }

    /**
     * Factory: retorna lista de regras para rehidratar Question.
     */
    public static List<DomainRule<String>> rehydrateQuestionIdRules() {
        return List.of(
                questionIdNotNull()
        );
    }

    public static List<DomainRule<String>> rehydrateQuestionLabelRules() {
        return List.of(
                questionLabelNotNull()
        );
    }

    public static List<DomainRule<ParameterizationStatus>> rehydrateQuestionStatusRules() {
        return List.of(
                questionStatusNotNull()
        );
    }

    /**
     * Regra: auditInfo deve estar presente (não nulo).
     */
    public static DomainRule<AuditInfo> auditInfoNotNull() {
        return new DomainRule<AuditInfo>() {
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
     * Factory: retorna lista de regras para validar auditInfo na rehidratação de Question.
     */
    public static List<DomainRule<AuditInfo>> rehydrateAuditInfoRules() {
        return List.of(
                auditInfoNotNull()
        );
    }
}
