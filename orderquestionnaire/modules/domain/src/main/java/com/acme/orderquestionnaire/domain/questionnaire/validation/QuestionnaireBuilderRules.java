package com.acme.orderquestionnaire.domain.questionnaire.validation;

import com.acme.orderquestionnaire.domain.questionnaire.errors.QuestionnaireDomainErrors;
import com.acme.shared.pattern.result.validation.DomainRule;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.vo.AuditInfo;
import java.util.List;

public final class QuestionnaireBuilderRules {

    private QuestionnaireBuilderRules() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Regra: order deve ser >= 0.
     */
    public static DomainRule<Integer> orderNotNegative() {
        return new DomainRule<Integer>() {
            @Override
            public boolean isSatisfiedBy(Integer candidate) {
                return candidate == null || candidate >= 0;
            }

            @Override
            public DomainError toDomainError() {
                return QuestionnaireDomainErrors.invalidOrder();
            }
        };
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
                return QuestionnaireDomainErrors.requiredObject("auditInfo");
            }
        };
    }

    /**
     * Factory: retorna lista de regras para validar order.
     */
    public static List<DomainRule<Integer>> orderValidationRules(Integer order) {
        return List.of(
                orderNotNegative()
        );
    }

    /**
     * Factory: retorna lista de regras para validar auditInfo.
     */
    public static List<DomainRule<AuditInfo>> auditInfoRules(AuditInfo auditInfo) {
        return List.of(
                auditInfoNotNull()
        );
    }
}
