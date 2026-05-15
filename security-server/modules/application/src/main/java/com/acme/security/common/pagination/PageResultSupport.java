package com.acme.security.common.pagination;

import com.acme.shared.engine.pagination.HybridPageRequest;
import com.acme.shared.engine.pagination.PageMode;
import com.acme.shared.engine.pagination.PageResult;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

public final class PageResultSupport {

    private PageResultSupport() {
        throw new IllegalStateException("Utility class");
    }

    public static <T> PageResult<T> paginate(List<T> source,
                                             HybridPageRequest pageRequest,
                                             Comparator<T> comparator) {
        List<T> ordered = new ArrayList<>(source == null ? List.of() : source);
        if (comparator != null) {
            ordered.sort(comparator);
        }

        if (pageRequest.mode() == PageMode.CURSOR) {
            int offset = decodeCursor(pageRequest.cursor());
            int start = Math.max(0, offset);
            int endExclusive = Math.min(start + pageRequest.size(), ordered.size());
            boolean hasNext = endExclusive < ordered.size();
            String nextCursor = hasNext ? String.valueOf(endExclusive) : null;
            List<T> content = ordered.subList(start, endExclusive);
            return PageResult.forCursor(content, pageRequest.size(), nextCursor, hasNext, pageRequest.sort());
        }

        int page = pageRequest.page();
        int size = pageRequest.size();
        int from = Math.min(page * size, ordered.size());
        int to = Math.min(from + size, ordered.size());
        long totalElements = ordered.size();
        int totalPages = size == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
        boolean first = page == 0;
        boolean last = totalPages == 0 || page >= totalPages - 1;

        List<T> content = ordered.subList(from, to);
        return PageResult.forPage(content, page, size, totalElements, totalPages, first, last, pageRequest.sort());
    }

    public static <T, R> PageResult<R> map(PageResult<T> source, Function<T, R> mapper) {
        List<R> mapped = source.content().stream().map(mapper).toList();

        if (source.mode() == PageMode.CURSOR) {
            return PageResult.forCursor(
                    mapped,
                    source.size(),
                    source.nextCursor(),
                    source.hasNext(),
                    source.appliedSort()
            );
        }

        return PageResult.forPage(
                mapped,
                source.page(),
                source.size(),
                source.totalElements(),
                source.totalPages(),
                Boolean.TRUE.equals(source.first()),
                Boolean.TRUE.equals(source.last()),
                source.appliedSort()
        );
    }

    private static int decodeCursor(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return 0;
        }

        try {
            return Math.max(0, Integer.parseInt(cursor));
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Invalid cursor value", exception);
        }
    }
}

