package com.acme.observability;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.acme.shared.pattern.result.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class LoggingAspect {
    private final ObjectMapper mapper;

    @Around("@within(com.acme.observability.Loggable) || @annotation(com.acme.observability.Loggable)")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        long startedAt = System.currentTimeMillis();
        var args = pjp.getArgs();
        String className = pjp.getSignature().getDeclaringTypeName();
        String methodName = pjp.getSignature().getName();

        log.info(map(
                "event", "method.start",
                "class", className,
                "method", methodName,
                "correlationId", MDC.get("correlationId"),
                "flowId", MDC.get("flowId"),
                "args", args
        ));

        try {
            var result = pjp.proceed();
            var successPayload = map(
                    "event", "method.success",
                    "class", className,
                    "method", methodName,
                    "correlationId", MDC.get("correlationId"),
                    "flowId", MDC.get("flowId"),
                    "durationMs", System.currentTimeMillis() - startedAt,
                    "result", result
            );
            if (result instanceof Result<?, ?> outcome && outcome.isFailure()) {
                log.warn(successPayload);
            } else {
                log.info(successPayload);
            }
            return result;
        } catch (Throwable throwable) {
            log.error(map(
                    "event", "method.error",
                    "class", className,
                    "method", methodName,
                    "correlationId", MDC.get("correlationId"),
                    "flowId", MDC.get("flowId"),
                    "durationMs", System.currentTimeMillis() - startedAt,
                    "error", throwable.getClass().getName(),
                    "message", throwable.getMessage()
            ));
            throw throwable;
        }
    }

    private String map(Object... kv) {
        try {
            var m = new java.util.LinkedHashMap<String, Object>();
            for (int i = 0; i < kv.length; i += 2) m.put(kv[i].toString(), kv[i + 1]);
            return mapper.writeValueAsString(m);
        } catch (Exception e) {
            return "{\"log\":\"serialization_error\"}";
        }
    }
}
