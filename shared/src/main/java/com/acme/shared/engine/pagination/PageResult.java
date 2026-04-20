package com.acme.shared.engine.pagination;

import java.util.List;
import java.util.Objects;

public record PageResult<T>(
        List<T> content,
        PageMode mode,
        Integer page,
        Integer size,
        Long totalElements,
        Integer totalPages,
        Boolean first,
        Boolean last,
        String nextCursor,
        boolean hasNext,
        List<SortSpec> appliedSort
) {

    public PageResult {
        Objects.requireNonNull(mode, "mode must not be null");

        if (size == null || size <= 0) {
            throw new IllegalArgumentException("size must be greater than zero");
        }

        content = content == null ? List.of() : List.copyOf(content);
        appliedSort = appliedSort == null ? List.of() : List.copyOf(appliedSort);

        if (mode == PageMode.PAGE) {
            if (page == null || page < 0) {
                throw new IllegalArgumentException("page must be greater than or equal to zero for page mode");
            }

            if (totalElements == null || totalElements < 0) {
                throw new IllegalArgumentException("totalElements must be greater than or equal to zero for page mode");
            }

            if (totalPages == null || totalPages < 0) {
                throw new IllegalArgumentException("totalPages must be greater than or equal to zero for page mode");
            }

            if (first == null || last == null) {
                throw new IllegalArgumentException("first and last must be informed for page mode");
            }
        }
    }

    public static <T> PageResult<T> forPage(
            List<T> content,
            int page,
            int size,
            long totalElements,
            int totalPages,
            boolean first,
            boolean last,
            List<SortSpec> appliedSort
    ) {
        return new PageResult<>(
                content,
                PageMode.PAGE,
                page,
                size,
                totalElements,
                totalPages,
                first,
                last,
                null,
                !last,
                appliedSort
        );
    }

    public static <T> PageResult<T> forCursor(
            List<T> content,
            int size,
            String nextCursor,
            boolean hasNext,
            List<SortSpec> appliedSort
    ) {
        return new PageResult<>(
                content,
                PageMode.CURSOR,
                null,
                size,
                null,
                null,
                null,
                null,
                nextCursor,
                hasNext,
                appliedSort
        );
    }
}

