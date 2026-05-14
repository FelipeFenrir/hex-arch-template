package com.acme.orderquestionnaire.adapters.in.rest.unit.error;

import com.acme.orderquestionnaire.adapters.in.rest.error.ApiErrorCodes;
import com.acme.orderquestionnaire.adapters.in.rest.error.GlobalExceptionHandler;
import com.acme.orderquestionnaire.adapters.in.rest.error.ProblemDetailsFactory;
import com.acme.shared.stereotypes.test.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@UnitTest
@DisplayName("GlobalExceptionHandler")
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler(new ProblemDetailsFactory());

    @Test
    @DisplayName("should map missing servlet request parameter to bad request problem details")
    void shouldMapMissingServletRequestParameterToBadRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/questionnaires/qn1");
        MissingServletRequestParameterException exception =
                new MissingServletRequestParameterException("channelId", "String");

        var response = handler.handleMissingRequestParameter(exception, request);
        var body = response.getBody();

        assertEquals(400, response.getStatusCode().value());
        assertEquals(MediaType.APPLICATION_PROBLEM_JSON, response.getHeaders().getContentType());
        assertNotNull(body);
        assertNotNull(body.getProperties());
        assertNotNull(body.getInstance());
        assertEquals("Bad request", body.getTitle());
        assertEquals(ApiErrorCodes.VALIDATION_BAD_REQUEST, body.getProperties().get("code"));
        assertEquals("/api/v1/questionnaires/qn1", body.getInstance().toString());
    }

    @Test
    @DisplayName("should map bind exception to validation error problem details")
    void shouldMapBindExceptionToValidationError() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/questionnaires");
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "searchQuestionnaireRequest");
        bindingResult.addError(new FieldError("searchQuestionnaireRequest", "size", "Failed to convert property value"));
        BindException exception = new BindException(bindingResult);

        var response = handler.handleBindException(exception, request);
        var body = response.getBody();

        assertEquals(400, response.getStatusCode().value());
        assertEquals(MediaType.APPLICATION_PROBLEM_JSON, response.getHeaders().getContentType());
        assertNotNull(body);
        assertNotNull(body.getProperties());
        assertEquals("Validation error", body.getTitle());
        assertEquals(ApiErrorCodes.VALIDATION_INVALID_PAYLOAD, body.getProperties().get("code"));
    }

    @Test
    @DisplayName("should map method argument type mismatch to bad request problem details")
    void shouldMapMethodArgumentTypeMismatchToBadRequest() throws NoSuchMethodException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/questionnaires");
        MethodParameter methodParameter = new MethodParameter(
                GlobalExceptionHandlerTest.class.getDeclaredMethod("sampleMethod", Integer.class),
                0
        );
        MethodArgumentTypeMismatchException exception = new MethodArgumentTypeMismatchException(
                "abc",
                Integer.class,
                "size",
                methodParameter,
                new IllegalArgumentException("For input string: \"abc\"")
        );

        var response = handler.handleMethodArgumentTypeMismatch(exception, request);
        var body = response.getBody();

        assertEquals(400, response.getStatusCode().value());
        assertEquals(MediaType.APPLICATION_PROBLEM_JSON, response.getHeaders().getContentType());
        assertNotNull(body);
        assertNotNull(body.getProperties());
        assertEquals("Bad request", body.getTitle());
        assertEquals(ApiErrorCodes.VALIDATION_BAD_REQUEST, body.getProperties().get("code"));
    }

    @Test
    @DisplayName("should map invalid uuid to bad request problem details")
    void shouldMapInvalidUuidToBadRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/questions");
        Exception exception = new InvalidUuidException("Invalid UUID: \"not-a-uuid\"");

        var response = handler.handleUnexpected(exception, request);
        var body = response.getBody();

        assertEquals(400, response.getStatusCode().value());
        assertEquals(MediaType.APPLICATION_PROBLEM_JSON, response.getHeaders().getContentType());
        assertNotNull(body);
        assertNotNull(body.getProperties());
        assertEquals("Bad request", body.getTitle());
        assertEquals(ApiErrorCodes.VALIDATION_BAD_REQUEST, body.getProperties().get("code"));
        assertEquals("/api/v1/questions", body.getInstance().toString());
    }

    @SuppressWarnings("unused")
    private void sampleMethod(Integer size) {
    }

    private static final class InvalidUuidException extends RuntimeException {
        private InvalidUuidException(String message) {
            super(message);
        }
    }
}








