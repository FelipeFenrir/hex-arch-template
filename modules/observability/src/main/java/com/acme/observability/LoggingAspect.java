package com.acme.observability;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class LoggingAspect {
    private final ObjectMapper mapper;

    @Around("@within(com.acme.observability.Loggable) || @annotation(com.acme.observability.Loggable)")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        var args = pjp.getArgs();
        log.info(map("event", "method.enter", "class", pjp.getSignature().getDeclaringTypeName(),
                "method", pjp.getSignature().getName(), "args", args));
        var result = pjp.proceed();
        log.info(map("event", "method.exit", "class", pjp.getSignature().getDeclaringTypeName(),
                "method", pjp.getSignature().getName(), "result", result));
        return result;
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
