package com.acme.orderquestionnaire.adapters.in.rest.error;

import com.acme.observability.Loggable;
import com.acme.shared.pattern.result.DomainError;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;

import java.util.List;
import java.util.Map;

@Component
@Loggable
public class ProblemDetailsFactory {

    public ProblemDetail fromDomainErrors(HttpServletRequest request, List<DomainError> domainErrors) {
        List<DomainError> safeErrors = domainErrors == null ? List.of() : List.copyOf(domainErrors);
        DomainError first = safeErrors.isEmpty() ? new DomainError("UNKNOWN", "unknown domain error") : safeErrors.getFirst();
        HttpStatus status = mapStatus(first.code());

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, first.message());
        problem.setTitle(titleFor(status));
        problem.setInstance(java.net.URI.create(relativeInstance(request)));
        problem.setProperty("code", ApiErrorCodes.domain(first.code()));
        problem.setProperty("errors", safeErrors.stream()
                .map(error -> Map.of("code", ApiErrorCodes.domain(error.code()), "message", error.message()))
                .toList());
        return problem;
    }

    public ProblemDetail fromValidationErrors(HttpServletRequest request, List<FieldError> fieldErrors) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "One or more fields are invalid.");
        problem.setTitle("Validation error");
        problem.setInstance(java.net.URI.create(relativeInstance(request)));
        problem.setProperty("code", ApiErrorCodes.VALIDATION_INVALID_PAYLOAD);
        problem.setProperty("errors", fieldErrors.stream()
                .map(error -> Map.of("field", error.getField(), "message", defaultMessage(error)))
                .toList());
        return problem;
    }

    public ProblemDetail fromSystemError(HttpServletRequest request, Throwable throwable) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected internal error occurred.");
        problem.setTitle("Internal server error");
        problem.setInstance(java.net.URI.create(relativeInstance(request)));
        problem.setProperty("code", ApiErrorCodes.SYSTEM_UNEXPECTED_ERROR);
        problem.setProperty("errors", List.of(Map.of(
                "type", throwable.getClass().getSimpleName(),
                "message", throwable.getMessage() == null ? "no message" : throwable.getMessage()
        )));
        return problem;
    }

    public ProblemDetail fromBadRequest(HttpServletRequest request, String detail, String code) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
        problem.setTitle("Bad request");
        problem.setInstance(java.net.URI.create(relativeInstance(request)));
        problem.setProperty("code", code);
        return problem;
    }

    public ProblemDetail fromMalformedBody(HttpServletRequest request, Throwable throwable) {
        String detail = throwable == null || throwable.getMessage() == null || throwable.getMessage().isBlank()
                ? "Request body is malformed or contains invalid values."
                : throwable.getMessage();

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
        problem.setTitle("Malformed request body");
        problem.setInstance(java.net.URI.create(relativeInstance(request)));
        problem.setProperty("code", ApiErrorCodes.VALIDATION_MALFORMED_BODY);
        problem.setProperty("errors", List.of(Map.of(
                "type", throwable == null ? "HttpMessageNotReadableException" : throwable.getClass().getSimpleName(),
                "message", detail
        )));
        return problem;
    }

    private HttpStatus mapStatus(String code) {
        if (code == null || code.isBlank()) {
            return HttpStatus.UNPROCESSABLE_ENTITY;
        }
        if (code.contains("NOT_FOUND")) {
            return HttpStatus.NOT_FOUND;
        }
        if (code.contains("ALREADY_EXISTS") || code.contains("IN_USE") || code.contains("TRANSITION")) {
            return HttpStatus.CONFLICT;
        }
        if (code.contains("INVALID")) {
            return HttpStatus.BAD_REQUEST;
        }
        return HttpStatus.UNPROCESSABLE_ENTITY;
    }

    private String relativeInstance(HttpServletRequest request) {
        if (request == null || request.getRequestURI() == null || request.getRequestURI().isBlank()) {
            return "/";
        }
        return request.getRequestURI();
    }

    private String titleFor(HttpStatus status) {
        return switch (status) {
            case BAD_REQUEST -> "Validation error";
            case NOT_FOUND -> "Resource not found";
            case CONFLICT -> "Business rule violation";
            default -> "Domain error";
        };
    }

    private String defaultMessage(FieldError error) {
        return error.getDefaultMessage() == null ? "invalid value" : error.getDefaultMessage();
    }
}
