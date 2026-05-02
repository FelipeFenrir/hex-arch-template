package com.acme.orderquestionnaire.application.unit.questionnaire.service.step;

import com.acme.orderquestionnaire.application.questionnaire.dto.command.ValidateQuestionnaireAnswersCommand;
import com.acme.orderquestionnaire.application.questionnaire.port.out.repository.QuestionnaireCommandOutPort;
import com.acme.orderquestionnaire.application.questionnaire.service.context.ValidateQuestionnaireAnswersPipelineContext;
import com.acme.orderquestionnaire.application.questionnaire.service.step.FetchQuestionnaireForAnswersValidationStep;
import com.acme.orderquestionnaire.domain.questionnaire.Questionnaire;
import com.acme.orderquestionnaire.domain.questionnaire.vo.QuestionnaireId;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;
import com.acme.shared.stereotypes.test.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@UnitTest
@DisplayName("FetchQuestionnaireForAnswersValidationStep")
class FetchQuestionnaireForAnswersValidationStepTest {

    @Test
    @DisplayName("should fetch and store questionnaire when found")
    void shouldFetchAndStoreQuestionnaireWhenFound() {
        QuestionnaireCommandOutPort outPort = mock(QuestionnaireCommandOutPort.class);
        Questionnaire questionnaire = mock(Questionnaire.class);
        QuestionnaireId id = QuestionnaireId.of("q_1", "APP", "J_1");
        when(outPort.findQuestionnaireById(id)).thenReturn(Optional.of(questionnaire));

        ValidateQuestionnaireAnswersCommand command = new ValidateQuestionnaireAnswersCommand("q_1", "APP", "J_1", java.util.Map.of());
        ValidateQuestionnaireAnswersPipelineContext context = new ValidateQuestionnaireAnswersPipelineContext(command);
        FetchQuestionnaireForAnswersValidationStep step = new FetchQuestionnaireForAnswersValidationStep(outPort);

        Result<Void, List<DomainError>> result = step.execute(context);

        assertInstanceOf(Result.Success.class, result);
        assertTrue(context.questionnaire() == questionnaire);
    }

    @Test
    @DisplayName("should fail when questionnaire does not exist")
    void shouldFailWhenQuestionnaireDoesNotExist() {
        QuestionnaireCommandOutPort outPort = mock(QuestionnaireCommandOutPort.class);
        when(outPort.findQuestionnaireById(QuestionnaireId.of("q_missing", "APP", "J_1")))
                .thenReturn(Optional.empty());

        ValidateQuestionnaireAnswersCommand command = new ValidateQuestionnaireAnswersCommand("q_missing", "APP", "J_1", java.util.Map.of());
        FetchQuestionnaireForAnswersValidationStep step = new FetchQuestionnaireForAnswersValidationStep(outPort);

        Result<Void, List<DomainError>> result = step.execute(new ValidateQuestionnaireAnswersPipelineContext(command));

        assertInstanceOf(Result.Failure.class, result);
        List<DomainError> errors = result.errorOrElseThrow(() -> new IllegalStateException("Expected failure"));
        assertTrue(errors.stream().anyMatch(error -> error.code().equals("QUESTIONNAIRE_NOT_FOUND")));
    }
}

