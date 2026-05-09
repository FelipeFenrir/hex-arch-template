package com.acme.orderquestionnaire.adapters.in.rest.unit.error;

import com.acme.orderquestionnaire.adapters.in.rest.error.ApiErrorCodes;
import com.acme.orderquestionnaire.adapters.in.rest.error.ProblemDetailsFactory;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.stereotypes.test.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@UnitTest
@DisplayName("ProblemDetailsFactory")
class ProblemDetailsFactoryTest {

    private final ProblemDetailsFactory factory = new ProblemDetailsFactory();

    @Test
    @DisplayName("should map domain not found to 404")
    void shouldMapDomainNotFoundTo404() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/questions/missing");

        var problem = factory.fromDomainErrors(request,
                List.of(new DomainError("QUESTION_NOT_FOUND", "question was not found")));

        assertNotNull(problem.getProperties());
        assertNotNull(problem.getInstance());
        assertEquals(404, problem.getStatus());
        assertEquals(ApiErrorCodes.domain("QUESTION_NOT_FOUND"), problem.getProperties().get("code"));
        assertEquals("/api/v1/questions/missing", problem.getInstance().toString());
    }

    @Test
    @DisplayName("should map bad request to VAL namespace")
    void shouldMapBadRequestToValidationNamespace() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/questions");

        var problem = factory.fromBadRequest(request, "invalid sort", ApiErrorCodes.VALIDATION_BAD_REQUEST);

        assertNotNull(problem.getProperties());
        assertEquals(400, problem.getStatus());
        assertEquals(ApiErrorCodes.VALIDATION_BAD_REQUEST, problem.getProperties().get("code"));
    }

    @Test
    @DisplayName("should map malformed body to dedicated validation code")
    void shouldMapMalformedBodyToDedicatedValidationCode() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/questions");

        var problem = factory.fromMalformedBody(request, new IllegalArgumentException("Invalid UUID string: test"));

        assertNotNull(problem.getProperties());
        assertNotNull(problem.getInstance());
        assertEquals(400, problem.getStatus());
        assertEquals(ApiErrorCodes.VALIDATION_MALFORMED_BODY, problem.getProperties().get("code"));
        assertEquals("/api/v1/questions", problem.getInstance().toString());
    }

    @Test
    @DisplayName("should map unexpected errors to SYS namespace")
    void shouldMapUnexpectedErrorsToSystemNamespace() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/questions");

        var problem = factory.fromSystemError(request, new IllegalStateException("boom"));

        assertNotNull(problem.getProperties());
        assertEquals(500, problem.getStatus());
        assertEquals(ApiErrorCodes.SYSTEM_UNEXPECTED_ERROR, problem.getProperties().get("code"));
    }
}
