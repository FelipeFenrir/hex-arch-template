package com.acme.observability;

import com.acme.observability.config.ObservabilityLoggingProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.acme.shared.pattern.result.Result;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;

@Aspect
public class LoggingAspect {
    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    private final ObjectMapper mapper;
    private final LogSanitizer sanitizer;
    private final ObservabilityLoggingProperties properties;

    public LoggingAspect(
            ObjectMapper mapper,
            LogSanitizer sanitizer,
            ObservabilityLoggingProperties properties
    ) {
        this.mapper = mapper;
        this.sanitizer = sanitizer;
        this.properties = properties;
    }

    @Around("@within(com.acme.observability.Loggable) || @annotation(com.acme.observability.Loggable)")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        long startedAt = System.currentTimeMillis();
        String className = pjp.getSignature().getDeclaringTypeName();
        String methodName = pjp.getSignature().getName();

        Map<String, Object> startPayload = basePayload("method.start", className, methodName);
        if (properties.isLogArguments()) {
            startPayload.put("args", sanitizer.sanitize(pjp.getArgs()));
        }
        log.info(write(startPayload));

        try {
            var result = pjp.proceed();

            Map<String, Object> successPayload = basePayload("method.success", className, methodName);
            successPayload.put("durationMs", System.currentTimeMillis() - startedAt);
            if (properties.isLogResult()) {
                successPayload.put("result", sanitizer.sanitize(result));
            }

            String successMessage = write(successPayload);
            if (result instanceof Result<?, ?> outcome && outcome.isFailure()) {
                log.warn(successMessage);
            } else {
                log.info(successMessage);
            }
            return result;
        } catch (Throwable throwable) {
            Map<String, Object> errorPayload = basePayload("method.error", className, methodName);
            errorPayload.put("durationMs", System.currentTimeMillis() - startedAt);
            errorPayload.put("error", throwable.getClass().getName());
            errorPayload.put("message", throwable.getMessage());

            log.error(write(errorPayload));
            throw throwable;
        }
    }

    private Map<String, Object> basePayload(String event, String className, String methodName) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("event", event);
        payload.put("class", className);
        payload.put("method", methodName);
        payload.put("correlationId", MDC.get("correlationId"));
        payload.put("flowId", MDC.get("flowId"));
        return payload;
    }

    private String write(Map<String, Object> payload) {
        try {
            return mapper.writeValueAsString(payload);
        } catch (Exception e) {
            return "{\"log\":\"serialization_error\"}";
        }
    }
}
