package com.acme.orderquestionnaire.application.question.service.step;

import com.acme.orderquestionnaire.application.question.service.context.DeleteQuestionPipelineContext;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;
import com.acme.shared.stereotypes.test.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

@UnitTest
@DisplayName("ValidateDeleteQuestionIdStep")
class ValidateDeleteQuestionIdStepTest {

    private final ValidateDeleteQuestionIdStep step = new ValidateDeleteQuestionIdStep();

    @Test
    @DisplayName("should fail when id is blank")
    void shouldFailWhenIdIsBlank() {
        Result<Void, List<DomainError>> result = step.execute(new DeleteQuestionPipelineContext("  "));

        assertInstanceOf(Result.Failure.class, result);
        List<DomainError> errors = result.errorOrElseThrow(() -> new IllegalStateException("Expected failure"));
        assertTrue(errors.stream().anyMatch(error -> error.code().equals("INVALID_ID")));
    }

    @Test
    @DisplayName("should succeed when id is valid")
    void shouldSucceedWhenIdIsValid() {
        Result<Void, List<DomainError>> result = step.execute(new DeleteQuestionPipelineContext("q_1"));

        assertInstanceOf(Result.Success.class, result);
    }
}

