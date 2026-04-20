package com.acme.shared.pattern.result.validation;

import com.acme.shared.engine.rule.GenericRule;
import com.acme.shared.pattern.result.DomainError;

/**
 * Encapsulates a domain validation rule and its functional error mapping.
 *
 * @param <T> candidate type being validated
 */
public interface DomainRule<T> {

    /**
     * Returns true when the candidate satisfies the rule.
     */
    boolean isSatisfiedBy(T candidate);

    /**
     * Returns the domain error produced when this rule is violated.
     */
    DomainError toDomainError();

    /**
     * Adapter to shared RuleEngine representation.
     */
    default GenericRule<T> toGenericRule() {
        return new GenericRule<>(this::isSatisfiedBy, this.toDomainError().message());
    }
}

