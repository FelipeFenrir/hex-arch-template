package com.acme.observability;

import io.opentelemetry.api.trace.Span;
import org.slf4j.MDC;

/**
 * Utility class for managing W3C trace context extraction for logging.
 * <p>
 * Inbound adapters (servlet filters) should call:
 * {@link #extractTraceContextToMdc()} after the request enters the Spring context
 * where OpenTelemetry trace context is active.
 * <p>
 * This ensures that:
 * - traceId and spanId are available in logs for correlation with Tempo traces
 * - all application logs include distributed trace information
 */
public final class TraceContextPropagator {

    public static final String TRACE_ID_KEY = "traceId";
    public static final String SPAN_ID_KEY = "spanId";

    private TraceContextPropagator() {
    }

    /**
     * Populates W3C Baggage with correlationId and flowId from the current context.
     * This is a no-op if OTel auto-instrumentation is not active (Spring Boot provides W3C propagation automatically).
     * <p>
     * Baggage propagation is handled by OTel's auto-instrumentation and W3C context propagators.
     * correlationId and flowId will be propagated as headers via logback MDC + servlet filters.
     */
    public static void populateBaggageFromContext() {
        // Baggage propagation is handled by OTel auto-instrumentation
        // No explicit action needed here
    }

    /**
     * Extracts the current span's traceId and spanId from OpenTelemetry context
     * and populates them into MDC so they appear in all subsequent log statements.
     * <p>
     * Must be called after the request enters Spring's servlet filter chain where
     * OpenTelemetry trace context is active. Typically called right after
     * CorrelationContext.populate() in the request boundary filter.
     */
    public static void extractTraceContextToMdc() {
        try {
            Span span = Span.current();
            if (span != null && span.isRecording()) {
                String traceId = span.getSpanContext().getTraceId();
                String spanId = span.getSpanContext().getSpanId();
                if (traceId != null && !traceId.isEmpty()) {
                    MDC.put(TRACE_ID_KEY, traceId);
                }
                if (spanId != null && !spanId.isEmpty()) {
                    MDC.put(SPAN_ID_KEY, spanId);
                }
            }
        } catch (Exception e) {
            // Graceful fallback if OTel is not available or misconfigured
            // Trace context simply won't be available in logs
        }
    }

    /**
     * Clears trace context from MDC to prevent context leakage in thread-pool environments.
     * Should be called in a finally block at the request boundary, after
     * CorrelationContext.clear().
     */
    public static void clearTraceContext() {
        MDC.remove(TRACE_ID_KEY);
        MDC.remove(SPAN_ID_KEY);
    }

    /**
     * Returns the current traceId from the MDC, or null if not set.
     */
    public static String traceId() {
        return MDC.get(TRACE_ID_KEY);
    }

    /**
     * Returns the current spanId from the MDC, or null if not set.
     */
    public static String spanId() {
        return MDC.get(SPAN_ID_KEY);
    }
}


