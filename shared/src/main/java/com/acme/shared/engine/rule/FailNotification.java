package com.acme.shared.engine.rule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class FailNotification {

    private final List<String> errors = new ArrayList<>();

    public void addError(String message) {
        errors.add(message);
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public List<String> getErrors() {
        return Collections.unmodifiableList(errors);
    }

    public void throwIfHasErrors(Function<List<String>, ? extends RuntimeException> exceptionFactory) {
        if (hasErrors()) {
            throw exceptionFactory.apply(errors);
        }
    }
}
