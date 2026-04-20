package com.acme.shared.engine.state;

import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;

import java.util.List;

@FunctionalInterface
public interface TransitionAction<Ctx> {
    Result<Ctx, List<DomainError>> apply(Ctx context);
}

