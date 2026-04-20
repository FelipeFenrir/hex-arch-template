package com.acme.orderquestionnaire.domain.question.validation;

import com.acme.orderquestionnaire.domain.question.errors.QuestionDomainErrors;
import com.acme.shared.pattern.result.validation.DomainRule;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.vo.AuditInfo;
import java.util.List;

public final class QuestionBuilderRules {

    private QuestionBuilderRules() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Regra: salesItemReferenceCode deve estar presente.
     */
    public static DomainRule<String> salesItemReferenceCodeNotNull() {
        return new DomainRule<String>() {
            @Override
            public boolean isSatisfiedBy(String candidate) {
                return candidate != null && !candidate.isBlank();
            }

            @Override
            public DomainError toDomainError() {
                return QuestionDomainErrors.requiredField("salesItemReferenceCode");
            }
        };
    }

    /**
     * Regra: auditInfo deve estar presente.
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
     * Factory: retorna lista de regras para validar salesItemReferenceCode.
     */
    public static List<DomainRule<String>> salesItemReferenceCodeRules(String salesItemRefCode) {
        return List.of(
                salesItemReferenceCodeNotNull()
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
