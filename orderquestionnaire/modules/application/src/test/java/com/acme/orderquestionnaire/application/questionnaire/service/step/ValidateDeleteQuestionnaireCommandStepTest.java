package com.acme.orderquestionnaire.application.questionnaire.service.step;

import com.acme.orderquestionnaire.application.questionnaire.dto.command.DeleteQuestionnaireCommand;
import com.acme.orderquestionnaire.application.questionnaire.service.context.DeleteQuestionnairePipelineContext;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;
import com.acme.shared.stereotypes.test.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

@UnitTest
@DisplayName("ValidateDeleteQuestionnaireCommandStep")
class ValidateDeleteQuestionnaireCommandStepTest {

    private final ValidateDeleteQuestionnaireCommandStep step = new ValidateDeleteQuestionnaireCommandStep();

    @Test
    @DisplayName("should fail when command is null")
    void shouldFailWhenCommandIsNull() {
        Result<Void, List<DomainError>> result = step.execute(new DeleteQuestionnairePipelineContext(null));

        assertInstanceOf(Result.Failure.class, result);
        List<DomainError> errors = result.errorOrElseThrow(() -> new IllegalStateException("Expected failure"));
        assertTrue(errors.stream().anyMatch(error -> error.code().equals("INVALID_COMMAND")));
    }

    @Test
    @DisplayName("should accumulate invalid composite id errors")
    void shouldAccumulateInvalidCompositeIdErrors() {
        DeleteQuestionnaireCommand command = new DeleteQuestionnaireCommand(" ", "APP", " ");

        Result<Void, List<DomainError>> result = step.execute(new DeleteQuestionnairePipelineContext(command));

        assertInstanceOf(Result.Failure.class, result);
        List<DomainError> errors = result.errorOrElseThrow(() -> new IllegalStateException("Expected failure"));
        assertTrue(errors.stream().anyMatch(error -> error.code().equals("INVALID_ID")));
        assertTrue(errors.stream().anyMatch(error -> error.code().equals("INVALID_JOURNEY_DISTRIBUTION_ID")));
    }
}

