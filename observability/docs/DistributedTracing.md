# Distributed Tracing Pattern (W3C + OpenTelemetry)

## Overview

This document describes the **required pattern** for implementing distributed tracing across all backend services in the ACME platform. This pattern is **automatically reusable** by any new service that follows the conventions.

---

## Architecture

```
┌─────────────────────────────────────────────┐
│ Inbound HTTP Request                        │
│ X-Correlation-Id: corr-123                  │
│ X-Flow-Id: login_flow                       │
└──────────────┬──────────────────────────────┘
               │
               ▼
┌──────────────────────────────────────────────┐
│ Servlet Filter (Request Boundary)            │
│ - Extract X-Correlation-Id / X-Flow-Id      │
│ - Echo headers in response                   │
│ - Populate MDC via CorrelationContext        │
│ - Extract traceId/spanId (OTel) to MDC      │
└──────────────┬──────────────────────────────┘
               │
               ▼
┌──────────────────────────────────────────────┐
│ Spring Application (OTel auto-instrumented)  │
│ - Span created automatically by OTel        │
│ - LoggingAspect enriches logs with MDC      │
│ - JSON logs include:                         │
│   • timestamp, service, level, logger        │
│   • correlationId, flowId                    │
│   • traceId, spanId                         │
│   • message (structured or free-text)        │
└──────────────┬──────────────────────────────┘
               │
               ▼
┌──────────────────────────────────────────────┐
│ Outbound Calls (HTTP/DB/etc.)                │
│ - W3C trace context propagated automatically │
│ - OpenTelemetry creates child spans         │
│ - All logs carry full context                │
└──────────────┬──────────────────────────────┘
               │
               ▼
┌──────────────────────────────────────────────┐
│ OTel Collector                               │
│ - Receives spans from apps via OTLP          │
│ - Enriches with service metadata             │
│ - Exports to Tempo                           │
└──────────────┬──────────────────────────────┘
               │
               ▼
┌──────────────────────────────────────────────┐
│ Tempo (Trace Backend)                        │
│ - Stores traces indexed by traceId           │
│ - Queryable via spanId, service, etc.        │
└──────────────────────────────────────────────┘
```

---

## Implementation Checklist for New Services

### 1. Logging Configuration (`logback-spring.xml`)

Every new service must use the **structured JSON pattern**:

```xml
<appender name="CONSOLE_JSON" class="ch.qos.logback.core.ConsoleAppender">
    <encoder class="net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder">
        <providers>
            <timestamp>
                <fieldName>timestamp</fieldName>
                <timeZone>UTC</timeZone>
            </timestamp>
            <pattern>
                <pattern>{"service":"${APP_NAME}","level":"%level","logger":"%logger{36}","thread":"%thread","correlationId":"%mdc{correlationId:-unknown}","flowId":"%mdc{flowId:-unknown}","traceId":"%mdc{traceId:-unknown}","spanId":"%mdc{spanId:-unknown}"}</pattern>
            </pattern>
            <message/>
            <mdc>
                <excludeMdcKeyName>correlationId</excludeMdcKeyName>
                <excludeMdcKeyName>flowId</excludeMdcKeyName>
            </mdc>
            <stackTrace>
                <fieldName>stackTrace</fieldName>
            </stackTrace>
        </providers>
    </encoder>
</appender>
```

**Key fields:**
- `service`: Application name (from `spring.application.name`)
- `correlationId`, `flowId`: From requests, via MDC
- `traceId`, `spanId`: From OpenTelemetry, via MDC
- `message`: Application log message (structured or free-form)

---

### 2. Servlet Filter (HTTP Request Boundary)

Every service **must** have a servlet filter that:

1. **Extracts or generates** `X-Correlation-Id` and `X-Flow-Id` headers
2. **Echoes them back** in the response
3. **Populates MDC** via `CorrelationContext.populate()`
4. **Extracts trace context** via `TraceContextPropagator.extractTraceContextToMdc()`
5. **Clears** both in a `finally` block

**Template:**

```java
@Component
public class MyAppHeadersFilter extends HttpFilter {

    @Override
    protected void doFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        
        String correlationId = headerOrGenerate(req, HeaderConstants.CORRELATION_HEADER);
        String flowId = req.getHeader(HeaderConstants.FLOW_HEADER);

        // Echo headers to client
        res.setHeader(HeaderConstants.CORRELATION_HEADER, correlationId);
        if (flowId != null && !flowId.isBlank()) {
            res.setHeader(HeaderConstants.FLOW_HEADER, flowId);
        }

        // Populate correlation context in MDC
        CorrelationContext.populate(correlationId, flowId);
        
        // Populate trace context in MDC (from OTel)
        TraceContextPropagator.populateBaggageFromContext();
        TraceContextPropagator.extractTraceContextToMdc();

        try {
            chain.doFilter(req, res);
        } finally {
            CorrelationContext.clear();
            TraceContextPropagator.clearTraceContext();
        }
    }

    private static String headerOrGenerate(HttpServletRequest req, String headerName) {
        var v = req.getHeader(headerName);
        return (v == null || v.isBlank()) ? UUID.randomUUID().toString() : v;
    }
}
```

---

### 3. Build Configuration

Add these dependencies to `modules/bootstrap/build.gradle`:

```groovy
dependencies {
    implementation "net.logstash.logback:logstash-logback-encoder:${logstashEncoderVersion}"
}
```

Add observability library to any adapter module that needs `TraceContextPropagator`:

```groovy
dependencies {
    implementation 'com.acme:observability'
}
```

---

### 4. Application Configuration (`application.yml`)

Ensure OTLP endpoint is configured:

```yaml
otel:
  exporter:
    otlp:
      endpoint: ${OTEL_EXPORTER_OTLP_ENDPOINT:http://localhost:4317}
  resource:
    attributes: service.name=myapp,service.namespace=acme
```

Spring Boot auto-configures OpenTelemetry instrumentation; no additional beans needed.

---

## Observable Flows in Grafana

Once a service follows this pattern, it automatically appears in:

### 1. **Service Map** (Tempo)
- Shows your service as a node
- Displays dependencies on other services
- Latency and error rates

### 2. **Logs** (Loki)
- Filter by `correlationId`, `flowId`, `traceId`, `spanId`
- Correlate logs across all services in a request flow

### 3. **Trace Investigation Dashboard**
- Click trace ID to see full latency breakdown
- Jump from trace to logs (and vice versa)
- Inspect individual spans by service

---

## Common Patterns

### Pattern A: Request → ServiceA → ServiceB → Database

```
Time  │ Service      │ Span ID       │ Correlation ID │ Status │ Message
──────┼──────────────┼───────────────┼────────────────┼────────┼─────────────
 001  │ api-gateway  │ span-root     │ corr-123       │ INFO   │ HTTP POST /orders
 002  │ orderquesti  │ span-aq       │ corr-123       │ INFO   │ Fetch questions
 003  │ mongo        │ span-mongo    │ corr-123       │ INFO   │ query executed
 004  │ orderquesti  │ span-aq       │ corr-123       │ INFO   │ Questions fetched
 005  │ api-gateway  │ span-root     │ corr-123       │ OK     │ Response sent (2ms)
```

All rows linked by **same `traceId`** (internal to OTel).
Filter dashboard by **`correlationId=corr-123`** to see all logs for that user request.

### Pattern B: BFF Aggregates Multiple Services

```
CorrelationId: corr-456
FlowId: checkout

┌─ [BFF] correlationId=corr-456, flowId=checkout
   ├─ [orderquestionnaire] questionnaire_id=q1 
   ├─ [security-server] tenant_id=t1
   └─ [wiremock] external_api_call
```

User can trace any step by filtering **`correlationId=corr-456` AND `flowId=checkout`**.

---

## Header Constants

Defined in `shared/src/main/java/com/acme/shared/constants/HeaderConstants.java`:

```java
public static final String CORRELATION_HEADER = "X-Correlation-Id";
public static final String FLOW_HEADER = "X-Flow-Id";
public static final String JOURNEY_HEADER = "X-Journey-Id";
public static final String CHANNEL_HEADER = "X-Channel-Id";
public static final String IDEMPOTENCY_HEADER = "X-Idempotency-Key";
```

---

## Testing

### Unit Test Template for Filter

```java
@Test
void shouldPopulateMdcAndExtractTraceContext() throws Exception {
    MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/endpoint");
    req.addHeader(HeaderConstants.CORRELATION_HEADER, "corr-123");
    req.addHeader(HeaderConstants.FLOW_HEADER, "test_flow");
    
    MockHttpServletResponse res = new MockHttpServletResponse();
    FilterChain chain = mock(FilterChain.class);
    
    doAnswer(inv -> {
        // Inside the filter chain, both contexts should be populated
        assertEquals("corr-123", CorrelationContext.correlationId());
        assertEquals("test_flow", CorrelationContext.flowId());
        assertNotNull(TraceContextPropagator.traceId()); // May be null if OTel inactive
        return null;
    }).when(chain).doFilter(any(), any());
    
    MyAppHeadersFilter filter = new MyAppHeadersFilter();
    filter.doFilter(req, res, chain);
    
    // After filter, contexts should be cleared
    assertNull(CorrelationContext.correlationId());
    assertNull(TraceContextPropagator.traceId());
}
```

### Integration Test

Run a full request flow through the app and verify:
1. Logs contain `correlationId`, `flowId`, `traceId`, `spanId`
2. Logs appear in Loki
3. Traces appear in Tempo
4. Grafana dashboards show the trace

---

## Troubleshooting

### Logs Missing traceId/spanId

**Cause:** OTel not initialized or Span not recording

**Fix:** 
- Verify `OTEL_EXPORTER_OTLP_ENDPOINT` is set
- Ensure `io.opentelemetry:opentelemetry-exporter-otlp` is on classpath
- Check Spring Boot boot actuator logs for OTel initialization

### Filter Not Called

**Cause:** Filter not registered as `@Component` or not in `FilterChain`

**Fix:**
- Add `@Component` to filter class
- Ensure filter extends `OncePerRequestFilter` or `HttpFilter`
- Check Spring startup logs for filter registration

### Baggage Not Propagated Downstream

This is expected; Spring Boot's default W3C propagators handle it automatically.
You do **not** need to explicitly manage W3C headers—OpenTelemetry does it for you.

---

## References

- **Spring Boot Auto-Configuration:** https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.observability.tracing
- **OpenTelemetry API:** https://opentelemetry.io/docs/instrumentation/java/
- **W3C Trace Context:** https://www.w3.org/TR/trace-context/
- **Logstash Logback Encoder:** https://github.com/logstash/logstash-logback-encoder


