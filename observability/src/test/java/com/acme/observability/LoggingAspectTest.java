package com.acme.observability;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.acme.observability.config.ObservabilityLoggingProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("LoggingAspect")
class LoggingAspectTest {

    private LoggingAspect loggingAspect;
    private ListAppender<ILoggingEvent> appender;
    private Logger logger;

    @BeforeEach
    void setUp() {
        ObservabilityLoggingProperties properties = new ObservabilityLoggingProperties();
        properties.setLogArguments(false);
        properties.setLogResult(true);

        ObjectMapper mapper = new ObjectMapper();
        LogSanitizer sanitizer = new LogSanitizer(mapper, properties);
        loggingAspect = new LoggingAspect(mapper, sanitizer, properties);

        logger = (Logger) LoggerFactory.getLogger(LoggingAspect.class);
        appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
    }

    @AfterEach
    void tearDown() {
        logger.detachAppender(appender);
    }

    @Test
    @DisplayName("should log method.success as ERROR for ResponseEntity 5xx")
    void shouldLogMethodSuccessAsErrorForResponseEntity5xx() throws Throwable {
        ProceedingJoinPoint pjp = mockJoinPoint(() -> ResponseEntity.internalServerError().body("boom"));

        loggingAspect.around(pjp);

        assertTrue(appender.list.stream()
                .anyMatch(event -> event.getLevel() == Level.ERROR
                        && event.getFormattedMessage().contains("\"event\":\"method.success\"")));
    }

    @Test
    @DisplayName("should log method.success as WARN for ResponseEntity 4xx")
    void shouldLogMethodSuccessAsWarnForResponseEntity4xx() throws Throwable {
        ProceedingJoinPoint pjp = mockJoinPoint(() -> ResponseEntity.badRequest().body("invalid"));

        loggingAspect.around(pjp);

        assertTrue(appender.list.stream()
                .anyMatch(event -> event.getLevel() == Level.WARN
                        && event.getFormattedMessage().contains("\"event\":\"method.success\"")));
    }

    @Test
    @DisplayName("should log method.success as WARN for validation problem detail code")
    void shouldLogMethodSuccessAsWarnForValidationProblemDetailCode() throws Throwable {
        ProceedingJoinPoint pjp = mockJoinPoint(() -> ResponseEntity.badRequest().body(problemDetail(HttpStatus.BAD_REQUEST, "VAL-001")));

        loggingAspect.around(pjp);

        assertTrue(appender.list.stream()
                .anyMatch(event -> event.getLevel() == Level.WARN
                        && event.getFormattedMessage().contains("\"event\":\"method.success\"")));
    }

    @Test
    @DisplayName("should log method.success as WARN for domain problem detail code")
    void shouldLogMethodSuccessAsWarnForDomainProblemDetailCode() throws Throwable {
        ProceedingJoinPoint pjp = mockJoinPoint(() -> ResponseEntity.unprocessableEntity().body(problemDetail(HttpStatus.UNPROCESSABLE_ENTITY, "DOM-QUESTION_NOT_FOUND")));

        loggingAspect.around(pjp);

        assertTrue(appender.list.stream()
                .anyMatch(event -> event.getLevel() == Level.WARN
                        && event.getFormattedMessage().contains("\"event\":\"method.success\"")));
    }

    @Test
    @DisplayName("should log method.success as ERROR for system problem detail code")
    void shouldLogMethodSuccessAsErrorForSystemProblemDetailCode() throws Throwable {
        ProceedingJoinPoint pjp = mockJoinPoint(() -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail(HttpStatus.INTERNAL_SERVER_ERROR, "SYS-001")));

        loggingAspect.around(pjp);

        assertTrue(appender.list.stream()
                .anyMatch(event -> event.getLevel() == Level.ERROR
                        && event.getFormattedMessage().contains("\"event\":\"method.success\"")));
    }

    @Test
    @DisplayName("should log domain-like exception as WARN")
    void shouldLogDomainLikeExceptionAsWarn() {
        ProceedingJoinPoint pjp = mockJoinPoint(() -> {
            throw new DomainResultException("business violation");
        });

        assertThrows(DomainResultException.class, () -> loggingAspect.around(pjp));

        assertTrue(appender.list.stream()
                .anyMatch(event -> event.getLevel() == Level.WARN
                        && event.getFormattedMessage().contains("\"event\":\"method.error\"")));
    }

    @Test
    @DisplayName("should log unexpected exception as ERROR")
    void shouldLogUnexpectedExceptionAsError() {
        ProceedingJoinPoint pjp = mockJoinPoint(() -> {
            throw new IllegalStateException("unexpected");
        });

        assertThrows(IllegalStateException.class, () -> loggingAspect.around(pjp));

        ILoggingEvent lastError = appender.list.stream()
                .filter(event -> event.getFormattedMessage().contains("\"event\":\"method.error\""))
                .reduce((ignored, second) -> second)
                .orElseThrow();

        assertEquals(Level.ERROR, lastError.getLevel());
    }

    private ProceedingJoinPoint mockJoinPoint(ThrowingSupplier supplier) {
        ProceedingJoinPoint pjp = mock(ProceedingJoinPoint.class);
        Signature signature = mock(Signature.class);

        try {
            when(pjp.proceed()).thenAnswer(ignoredInvocation -> supplier.get());
        } catch (Throwable throwable) {
            throw new IllegalStateException(throwable);
        }

        when(pjp.getSignature()).thenReturn(signature);
        when(signature.getDeclaringTypeName()).thenReturn("com.acme.Test");
        when(signature.getName()).thenReturn("testMethod");
        when(pjp.getArgs()).thenReturn(new Object[0]);
        return pjp;
    }

    private ProblemDetail problemDetail(HttpStatus status, String code) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(status);
        problemDetail.setProperty("code", code);
        return problemDetail;
    }

    @FunctionalInterface
    private interface ThrowingSupplier {
        Object get() throws Throwable;
    }

    private static final class DomainResultException extends RuntimeException {
        private DomainResultException(String message) {
            super(message);
        }
    }
}


