package com.acme.shared.pattern.result.validation;

import com.acme.shared.engine.rule.RuleValidator;
import com.acme.shared.pattern.result.DomainError;

import java.util.ArrayList;
import java.util.List;

/**
 * Executes DomainRule collections and returns functional errors.
 */
public final class DomainRuleRunner {

    private DomainRuleRunner() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Validates a candidate against all rules and accumulates DomainError violations.
     */
    public static <T> List<DomainError> validate(T candidate, List<DomainRule<T>> rules) {
        if (rules == null || rules.isEmpty()) {
            return List.of();
        }

        var genericRules = rules.stream().map(DomainRule::toGenericRule).toList();
        var notification = new RuleValidator<T>().addSpecs(genericRules).validate(candidate);

        if (!notification.hasErrors()) {
            return List.of();
        }

        List<DomainError> domainErrors = new ArrayList<>();
        for (DomainRule<T> rule : rules) {
            if (!rule.isSatisfiedBy(candidate)) {
                domainErrors.add(rule.toDomainError());
            }
        }
        return domainErrors;
    }
}

