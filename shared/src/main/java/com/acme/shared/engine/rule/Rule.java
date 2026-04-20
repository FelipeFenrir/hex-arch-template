package com.acme.shared.engine.rule;

@FunctionalInterface
public interface Rule<T> {

    boolean isSatisfiedBy(T candidate);

    default Rule<T> and(Rule<T> other) {
        return candidate -> this.isSatisfiedBy(candidate) && other.isSatisfiedBy(candidate);
    }

    default Rule<T> or(Rule<T> other) {
        return candidate -> this.isSatisfiedBy(candidate) || other.isSatisfiedBy(candidate);
    }

    default Rule<T> not() {
        return candidate -> !this.isSatisfiedBy(candidate);
    }
}