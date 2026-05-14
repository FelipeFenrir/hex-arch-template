package com.acme.orderquestionnaire.adapters.in.rest.question.request;

import com.acme.orderquestionnaire.application.question.dto.queries.SearchQuestionByFilter;
import com.acme.shared.engine.pagination.HybridPageRequest;
import com.acme.shared.engine.pagination.PageMode;
import com.acme.shared.engine.pagination.SortDirection;
import com.acme.shared.engine.pagination.SortSpec;
import com.acme.shared.enumerator.ParameterizationStatus;

import java.util.List;
import java.util.Locale;

public record SearchQuestionRequest(
        List<String> ids,
        List<String> salesItemReferenceCodes,
        String labelContains,
        ParameterizationStatus status,
        PageMode mode,
        Integer page,
        Integer size,
        String cursor,
        List<String> sort
) {
    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;

    public SearchQuestionByFilter toQuery() {
        return new SearchQuestionByFilter(
                ids,
                salesItemReferenceCodes,
                labelContains,
                status,
                null,
                toPageRequest()
        );
    }

    private HybridPageRequest toPageRequest() {
        int resolvedSize = size == null ? DEFAULT_SIZE : size;
        List<SortSpec> parsedSort = parseSort(sort);
        PageMode resolvedMode = mode == null ? PageMode.PAGE : mode;

        if (resolvedMode == PageMode.CURSOR) {
            if (page != null) {
                throw new IllegalArgumentException("page must not be informed when mode=CURSOR");
            }

            if (cursor == null || cursor.isBlank()) {
                return HybridPageRequest.ofCursorStart(resolvedSize, parsedSort);
            }

            return HybridPageRequest.ofCursor(cursor, resolvedSize, parsedSort);
        }

        if (cursor != null && !cursor.isBlank()) {
            throw new IllegalArgumentException("cursor must not be informed when mode=PAGE");
        }

        int resolvedPage = page == null ? DEFAULT_PAGE : page;
        return HybridPageRequest.ofPage(resolvedPage, resolvedSize, parsedSort);
    }

    private List<SortSpec> parseSort(List<String> values) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }

        return values.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(this::toSortSpec)
                .toList();
    }

    private SortSpec toSortSpec(String value) {
        String[] tokens = value.split(",", 2);
        String field = tokens[0].trim();
        SortDirection direction = tokens.length > 1
                ? SortDirection.valueOf(tokens[1].trim().toUpperCase(Locale.ROOT))
                : SortDirection.ASC;
        return new SortSpec(field, direction);
    }
}

