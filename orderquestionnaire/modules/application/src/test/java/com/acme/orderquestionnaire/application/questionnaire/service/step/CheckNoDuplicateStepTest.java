package com.acme.orderquestionnaire.application.questionnaire.service.step;

import com.acme.orderquestionnaire.application.questionnaire.dto.command.CreateQuestionnaireCommand;
import com.acme.orderquestionnaire.application.questionnaire.port.out.repository.QuestionnaireCommandOutPort;
import com.acme.orderquestionnaire.application.questionnaire.service.context.CreateQuestionnairePipelineContext;
import com.acme.orderquestionnaire.domain.questionnaire.vo.QuestionnaireId;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;
import com.acme.shared.stereotypes.test.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@UnitTest
@DisplayName("CheckNoDuplicateStep")
class CheckNoDuplicateStepTest {

    @Test
    @DisplayName("should succeed when questionnaire does not exist yet")
    void shouldSucceedWhenNoDuplicate() {
        QuestionnaireCommandOutPort outPort = mock(QuestionnaireCommandOutPort.class);
        when(outPort.existsById(QuestionnaireId.of("q_001", "APP", "JOURNEY_01"))).thenReturn(false);

        CheckNoDuplicateStep step = new CheckNoDuplicateStep(outPort);
        Result<Void, List<DomainError>> result = step.execute(context("q_001"));

        assertInstanceOf(Result.Success.class, result);
    }

    @Test
    @DisplayName("should fail with QUESTIONNAIRE_ALREADY_EXISTS when duplicate is found")
    void shouldFailWhenDuplicateExists() {
        QuestionnaireCommandOutPort outPort = mock(QuestionnaireCommandOutPort.class);
        when(outPort.existsById(QuestionnaireId.of("q_001", "APP", "JOURNEY_01"))).thenReturn(true);

        CheckNoDuplicateStep step = new CheckNoDuplicateStep(outPort);
        Result<Void, List<DomainError>> result = step.execute(context("q_001"));

        assertInstanceOf(Result.Failure.class, result);
        List<DomainError> errors = result.errorOrElseThrow(() -> new IllegalStateException("Expected failure"));
        assertTrue(errors.stream().anyMatch(e -> e.code().equals("QUESTIONNAIRE_ALREADY_EXISTS")));
    }

    private static CreateQuestionnairePipelineContext context(String id) {
        var cmd = new CreateQuestionnaireCommand(id, "APP", "JOURNEY_01", "Desc", null, null);
        return new CreateQuestionnairePipelineContext(cmd);
    }
}

