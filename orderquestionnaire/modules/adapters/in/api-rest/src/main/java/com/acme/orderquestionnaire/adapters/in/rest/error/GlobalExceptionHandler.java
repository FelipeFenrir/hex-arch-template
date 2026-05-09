package com.acme.orderquestionnaire.adapters.in.rest.error;

import com.acme.observability.Loggable;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
@Loggable
public class GlobalExceptionHandler {

    private final ProblemDetailsFactory problemDetailsFactory;

    public GlobalExceptionHandler(ProblemDetailsFactory problemDetailsFactory) {
        this.problemDetailsFactory = problemDetailsFactory;
    }

    @ExceptionHandler(DomainResultException.class)
    public ResponseEntity<ProblemDetail> handleDomainResult(DomainResultException exception, HttpServletRequest request) {
        return problem(problemDetailsFactory.fromDomainErrors(request, exception.errors()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidation(MethodArgumentNotValidException exception,
                                                          HttpServletRequest request) {
        return problem(problemDetailsFactory.fromValidationErrors(request,
                exception.getBindingResult().getFieldErrors()));
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ProblemDetail> handleBindException(BindException exception,
                                                             HttpServletRequest request) {
        return problem(problemDetailsFactory.fromValidationErrors(request, exception.getBindingResult().getFieldErrors()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> handleIllegalArgument(IllegalArgumentException exception,
                                                               HttpServletRequest request) {
        return problem(problemDetailsFactory.fromBadRequest(
                request,
                exception.getMessage(),
                ApiErrorCodes.VALIDATION_BAD_REQUEST
        ));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleMalformedBody(HttpMessageNotReadableException exception,
                                                             HttpServletRequest request) {
        return problem(problemDetailsFactory.fromMalformedBody(request, exception.getMostSpecificCause()));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ProblemDetail> handleMissingRequestParameter(MissingServletRequestParameterException exception,
                                                                       HttpServletRequest request) {
        return problem(problemDetailsFactory.fromBadRequest(
                request,
                exception.getMessage(),
                ApiErrorCodes.VALIDATION_BAD_REQUEST
        ));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ProblemDetail> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException exception,
                                                                          HttpServletRequest request) {
        return problem(problemDetailsFactory.fromBadRequest(
                request,
                exception.getMessage(),
                ApiErrorCodes.VALIDATION_BAD_REQUEST
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleUnexpected(Exception exception, HttpServletRequest request) {
        if (isInvalidUuidException(exception)) {
            return problem(problemDetailsFactory.fromBadRequest(
                    request,
                    exception.getMessage(),
                    ApiErrorCodes.VALIDATION_BAD_REQUEST
            ));
        }
        return problem(problemDetailsFactory.fromSystemError(request, exception));
    }

    private boolean isInvalidUuidException(Exception exception) {
        return exception != null && "InvalidUuidException".equals(exception.getClass().getSimpleName());
    }

    private ResponseEntity<ProblemDetail> problem(ProblemDetail problemDetail) {
        return ResponseEntity.status(problemDetail.getStatus())
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problemDetail);
    }
}
