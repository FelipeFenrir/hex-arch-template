package com.acme.security.common.api;

import java.util.List;

public record ApiCollectionResponse<T>(
        List<T> data,
        ApiMeta meta,
        ApiLinks links
) {
    public ApiCollectionResponse {
        data = data == null ? List.of() : List.copyOf(data);
    }

    public static <T> ApiCollectionResponse<T> of(List<T> data, ApiMeta meta, ApiLinks links) {
        return new ApiCollectionResponse<>(data, meta, links);
    }
}

