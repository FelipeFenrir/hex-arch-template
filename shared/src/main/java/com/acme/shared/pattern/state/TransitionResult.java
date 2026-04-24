package com.acme.shared.pattern.state;

public record TransitionResult<S, Ctx>(
        S targetState,
        Ctx context
) { }

