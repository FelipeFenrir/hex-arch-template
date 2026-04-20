package com.acme.shared.engine.state;

import com.acme.shared.engine.rule.GenericRule;

import java.util.List;

public record Transition<S, Ctx>(
        S from,
        S to,
        List<GenericRule<Ctx>> guards,
        List<TransitionAction<Ctx>> actions
) {

    public Transition(S from, S to) {
        this(from, to, List.of(), List.of());
    }
}

