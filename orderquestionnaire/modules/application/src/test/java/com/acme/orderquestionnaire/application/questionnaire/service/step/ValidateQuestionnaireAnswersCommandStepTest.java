package com.acme.orderquestionnaire.application.questionnaire.service.step;

import com.acme.orderquestionnaire.application.questionnaire.dto.command.ValidateQuestionnaireAnswersCommand;
import com.acme.orderquestionnaire.application.questionnaire.service.context.ValidateQuestionnaireAnswersPipelineContext;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;
import com.acme.shared.stereotypes.test.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

@UnitTest
@DisplayName("ValidateQuestionnaireAnswersCommandStep")
class ValidateQuestionnaireAnswersCommandStepTest {

    private final ValidateQuestionnaireAnswersCommandStep step = new ValidateQuestionnaireAnswersCommandStep();

    @Test
    @DisplayName("should fail when command is null")
    void shouldFailWhenCommandIsNull() {
        Result<Void, List<DomainError>> result = step.execute(new ValidateQuestionnaireAnswersPipelineContext(null));

        assertInstanceOf(Result.Failure.class, result);
        List<DomainError> errors = result.errorOrElseThrow(() -> new IllegalStateException("Expected failure"));
        assertTrue(errors.stream().anyMatch(error -> error.code().equals("INVALID_COMMAND")));
    }

    @Test
    @DisplayName("should fail when answers map is null")
    void shouldFailWhenAnswersMapIsNull() {
        ValidateQuestionnaireAnswersCommand command = new ValidateQuestionnaireAnswersCommand("q_1", "APP", "J_1", null);

        Result<Void, List<DomainError>> result = step.execute(new ValidateQuestionnaireAnswersPipelineContext(command));

        assertInstanceOf(Result.Failure.class, result);
        List<DomainError> errors = result.errorOrElseThrow(() -> new IllegalStateException("Expected failure"));
        assertTrue(errors.stream().anyMatch(error -> error.code().equals("INVALID_ANSWERS")));
    }

    @Test
    @DisplayName("should succeed for valid command")
    void shouldSucceedForValidCommand() {
        ValidateQuestionnaireAnswersCommand command = new ValidateQuestionnaireAnswersCommand("q_1", "APP", "J_1", Map.of());

        Result<Void, List<DomainError>> result = step.execute(new ValidateQuestionnaireAnswersPipelineContext(command));

        assertInstanceOf(Result.Success.class, result);
    }
}

