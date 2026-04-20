package com.acme.shared.engine.pagination;

import java.util.Objects;

public record SortSpec(String field, SortDirection direction) {

    public SortSpec {
        if (field == null || field.isBlank()) {
            throw new IllegalArgumentException("sort field must not be null or blank");
        }

        Objects.requireNonNull(direction, "sort direction must not be null");
    }
}

