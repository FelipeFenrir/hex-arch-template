package com.acme.shared.engine.rule;

import java.util.function.Predicate;

public record GenericRule<T>(Predicate<T> predicate, String message) implements Rule<T> {

    @Override
    public boolean isSatisfiedBy(T candidate) {
        return predicate.test(candidate);
    }
}
