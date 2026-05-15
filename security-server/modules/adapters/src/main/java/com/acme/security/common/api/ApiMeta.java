package com.acme.security.common.api;

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

