package com.acme.shared.engine.pagination;

import java.util.List;

public record HybridPageRequest(
        Integer page,
        Integer size,
        String cursor,
        List<SortSpec> sort,
        PageMode mode
) {

    public HybridPageRequest(Integer page,
                             Integer size,
                             String cursor,
                             List<SortSpec> sort) {
        this(page, size, cursor, sort, null);
    }

    public HybridPageRequest {
        if (size == null || size <= 0) {
            throw new IllegalArgumentException("size must be greater than zero");
        }

        var hasCursor = cursor != null && !cursor.isBlank();
        PageMode resolvedMode = mode == null ? (hasCursor ? PageMode.CURSOR : PageMode.PAGE) : mode;

        if (resolvedMode == PageMode.CURSOR) {
            if (page != null) {
                throw new IllegalArgumentException("page must be null when cursor is informed");
            }
        }

        if (resolvedMode == PageMode.PAGE) {
            if (page == null || page < 0) {
                throw new IllegalArgumentException("page must be greater than or equal to zero when mode is PAGE");
            }

            if (hasCursor) {
                throw new IllegalArgumentException("cursor must be blank when mode is PAGE");
            }
        }

        mode = resolvedMode;

        sort = sort == null ? List.of() : List.copyOf(sort);
    }

    public static HybridPageRequest ofPage(int page, int size, List<SortSpec> sort) {
        return new HybridPageRequest(page, size, null, sort, PageMode.PAGE);
    }

    public static HybridPageRequest ofCursor(String cursor, int size, List<SortSpec> sort) {
        return new HybridPageRequest(null, size, cursor, sort, PageMode.CURSOR);
    }

    public static HybridPageRequest ofCursorStart(int size, List<SortSpec> sort) {
        return new HybridPageRequest(null, size, null, sort, PageMode.CURSOR);
    }
}

