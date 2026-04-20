package com.acme.shared.pattern.result;

import java.util.ArrayList;
import java.util.List;

public final class Guard {

    private Guard() {
        throw new IllegalStateException("Utility class");
    }

    public static Result<Void, List<DomainError>> requireNonNull(Object value, ErrorCatalog error) {
        return value == null ? error.asFailure() : Result.success(null);
    }

    public static Result<Void, List<DomainError>> requireNonBlank(String value, ErrorCatalog error) {
        return value == null || value.isBlank() ? error.asFailure() : Result.success(null);
    }

    /**
     * Collect and accumulate errors from multiple independent validation results.
     * Returns success only if all validations passed, otherwise failure with all accumulated errors.
     * Useful for Phase 1 of 2-phase validation: accumulate independent pre-validations,
     * then proceed with fail-fast dependent rules and I/O.
     *
     * @param validations list of independent validation results
     * @return Result.success(null) if all validations passed, Result.failure(accumulated errors) otherwise
     */
    public static Result<Void, List<DomainError>> collect(List<Result<Void, List<DomainError>>> validations) {
        List<DomainError> accumulated = new ArrayList<>();
        for (Result<Void, List<DomainError>> validation : validations) {
            if (validation instanceof Result.Failure<Void, List<DomainError>>(List<DomainError> errors)) {
                accumulated.addAll(errors);
            }
        }
        return accumulated.isEmpty() ? Result.success(null) : Result.failure(accumulated);
    }
}
