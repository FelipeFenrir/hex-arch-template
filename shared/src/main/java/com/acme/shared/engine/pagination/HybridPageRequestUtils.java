package com.acme.shared.engine.pagination;

import java.util.List;
import java.util.Objects;

public final class HybridPageRequestUtils {

    private HybridPageRequestUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static boolean isCursorMode(HybridPageRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return request.mode() == PageMode.CURSOR;
    }

    public static HybridPageRequest resolveOrDefault(HybridPageRequest request,
                                                     int defaultPage,
                                                     int defaultSize,
                                                     List<SortSpec> defaultSort) {
        return request == null
                ? HybridPageRequest.ofPage(defaultPage, defaultSize, defaultSort)
                : request;
    }
}

