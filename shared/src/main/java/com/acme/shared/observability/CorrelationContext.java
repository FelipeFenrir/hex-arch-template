package com.acme.shared.observability;

import org.slf4j.MDC;

import java.util.UUID;

/**
 * Utility class for managing correlation and flow context in the MDC (Mapped Diagnostic Context).
 * <p>
 * Inbound adapters (REST filters, SQS listeners, gRPC interceptors) should call
 * {@link #populate(String, String)} at the entry point and {@link #clear()} in a
 * {@code finally} block to avoid context leakage between requests.
 * <pre>{@code
 *   try {
 *       CorrelationContext.populate(correlationId, flowId);
 *       chain.doFilter(req, res);
 *   } finally {
 *       CorrelationContext.clear();
 *   }
 * }</pre>
 */
public final class CorrelationContext {

    public static final String CORRELATION_ID_KEY = "correlationId";
    public static final String FLOW_ID_KEY = "flowId";

    private CorrelationContext() {
    }

    /**
     * Populates the MDC with the given correlation and flow identifiers.
     * If {@code correlationId} is blank or {@code null}, a new random UUID is generated.
     * If {@code flowId} is blank or {@code null}, the key is not set.
     */
    public static void populate(String correlationId, String flowId) {
        MDC.put(CORRELATION_ID_KEY, isBlank(correlationId)
                ? UUID.randomUUID().toString()
                : correlationId);
        if (!isBlank(flowId)) {
            MDC.put(FLOW_ID_KEY, flowId);
        }
    }

    /**
     * Returns the current correlation ID from the MDC, or {@code null} if not set.
     */
    public static String correlationId() {
        return MDC.get(CORRELATION_ID_KEY);
    }

    /**
     * Returns the current flow ID from the MDC, or {@code null} if not set.
     */
    public static String flowId() {
        return MDC.get(FLOW_ID_KEY);
    }

    /**
     * Removes correlation and flow keys from the MDC.
     */
    public static void clear() {
        MDC.remove(CORRELATION_ID_KEY);
        MDC.remove(FLOW_ID_KEY);
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}

