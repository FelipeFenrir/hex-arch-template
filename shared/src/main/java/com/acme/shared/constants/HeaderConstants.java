package com.acme.shared.constants;

public final class HeaderConstants {
    public static final String CORRELATION_HEADER = "X-Correlation-Id";
    public static final String FLOW_HEADER = "X-Flow-Id";
    public static final String JOURNEY_HEADER = "X-Journey-Id";
    public static final String CHANNEL_HEADER = "X-Channel-Id";
    public static final String IDEMPOTENCY_HEADER = "X-Idempotency-Key";

    private HeaderConstants() {
        throw new IllegalStateException("Utility class");
    }
}
