package com.acme.orderquestionnaire.adapters.in.rest.common;

public record ApiMeta(
        String mode,
        Integer page,
        Integer size,
        Long totalItems,
        Integer totalPages,
        Boolean hasNext,
        String nextCursor
) {
}

