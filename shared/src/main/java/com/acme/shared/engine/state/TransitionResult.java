package com.acme.shared.engine.state;

public record TransitionResult<S, Ctx>(
        S targetState,
        Ctx context
) { }

