package com.acme.observability;

import com.acme.observability.config.ObservabilityLoggingProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.acme.shared.pattern.result.Result;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.slf4j.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;

@Aspect
public class LoggingAspect {
    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);
    private static final String DOMAIN_RESULT_EXCEPTION = "DomainResultException";
    private static final String VALIDATION_PREFIX = "VAL-";
    private static final String DOMAIN_PREFIX = "DOM-";
    private static final String SYSTEM_PREFIX = "SYS-";

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
            switch (resolveSuccessLevel(result)) {
                case WARN -> log.warn(successMessage);
                case ERROR -> log.error(successMessage);
                default -> log.info(successMessage);
            }
            return result;
        } catch (Throwable throwable) {
            Map<String, Object> errorPayload = basePayload("method.error", className, methodName);
            errorPayload.put("durationMs", System.currentTimeMillis() - startedAt);
            errorPayload.put("error", throwable.getClass().getName());
            errorPayload.put("message", throwable.getMessage());

            String errorMessage = write(errorPayload);
            if (isExpectedWarningException(throwable)) {
                log.warn(errorMessage);
            } else {
                log.error(errorMessage);
            }
            throw throwable;
        }
    }

    private LogLevel resolveSuccessLevel(Object result) {
        if (result instanceof Result<?, ?> outcome && outcome.isFailure()) {
            return LogLevel.WARN;
        }

        if (result instanceof ResponseEntity<?> responseEntity) {
            LogLevel problemDetailLevel = resolveProblemDetailLevel(responseEntity.getBody());
            if (problemDetailLevel != null) {
                return problemDetailLevel;
            }

            int status = responseEntity.getStatusCode().value();
            if (status >= 500) {
                return LogLevel.ERROR;
            }
            if (status >= 400) {
                return LogLevel.WARN;
            }
        }

        return LogLevel.INFO;
    }

    private LogLevel resolveProblemDetailLevel(Object body) {
        if (!(body instanceof ProblemDetail problemDetail)) {
            return null;
        }

        Object codeValue = problemDetail.getProperties() == null ? null : problemDetail.getProperties().get("code");
        if (!(codeValue instanceof String code) || code.isBlank()) {
            return null;
        }

        if (code.startsWith(VALIDATION_PREFIX) || code.startsWith(DOMAIN_PREFIX)) {
            return LogLevel.WARN;
        }

        if (code.startsWith(SYSTEM_PREFIX)) {
            return LogLevel.ERROR;
        }

        return null;
    }

    private boolean isExpectedWarningException(Throwable throwable) {
        return throwable instanceof IllegalArgumentException
                || throwable instanceof MethodArgumentNotValidException
                || throwable instanceof HttpMessageNotReadableException
                || DOMAIN_RESULT_EXCEPTION.equals(throwable.getClass().getSimpleName());
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

    private enum LogLevel {
        INFO,
        WARN,
        ERROR
    }
}
