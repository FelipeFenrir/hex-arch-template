package com.acme.shared.engine.pagination;

import java.util.List;

public record HybridPageRequest(
        Integer page,
        Integer size,
        String cursor,
        List<SortSpec> sort
) {

    public HybridPageRequest {
        if (size == null || size <= 0) {
            throw new IllegalArgumentException("size must be greater than zero");
        }

        var hasCursor = cursor != null && !cursor.isBlank();

        if (hasCursor && page != null) {
            throw new IllegalArgumentException("page must be null when cursor is informed");
        }

        if (!hasCursor) {
            if (page == null || page < 0) {
                throw new IllegalArgumentException("page must be greater than or equal to zero when cursor is not informed");
            }
        }

        sort = sort == null ? List.of() : List.copyOf(sort);
    }

    public static HybridPageRequest ofPage(int page, int size, List<SortSpec> sort) {
        return new HybridPageRequest(page, size, null, sort);
    }

    public static HybridPageRequest ofCursor(String cursor, int size, List<SortSpec> sort) {
        return new HybridPageRequest(null, size, cursor, sort);
    }
}

