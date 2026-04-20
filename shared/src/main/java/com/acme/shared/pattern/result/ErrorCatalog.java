package com.acme.shared.pattern.result;

import java.util.List;

public interface ErrorCatalog {

    DomainError toDomainError();

    default DomainError toDomainError(Object... args) {
        return toDomainError();
    }

    default <V> Result<V, List<DomainError>> asFailure() {
        return Result.failure(List.of(toDomainError()));
    }

    default <V> Result<V, List<DomainError>> asFailure(Object... args) {
        return Result.failure(List.of(toDomainError(args)));
    }
}