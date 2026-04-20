package com.acme.shared.engine.rule;

import java.util.ArrayList;
import java.util.List;

public class RuleValidator<T> {
    List<GenericRule<T>> specs = new ArrayList<>();

    public RuleValidator<T> addSpec(GenericRule<T> spec) {
        specs.add(spec);
        return this;
    }

    public RuleValidator<T> addSpecs(List<GenericRule<T>> specs) {
        specs.forEach(this::addSpec);
        return this;
    }

    public FailNotification validate(T target) {
        FailNotification notification = new FailNotification();

        specs.stream()
                .filter(spec -> !spec.isSatisfiedBy(target))
                .map(GenericRule::message)
                .forEach(notification::addError);

        return notification;
    }
}
