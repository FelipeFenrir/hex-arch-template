package com.acme.orderquestionnaire.application.unit.questionnaire.service.step;

import com.acme.orderquestionnaire.application.questionnaire.dto.command.UpdateQuestionnaireCommand;
import com.acme.orderquestionnaire.application.questionnaire.port.out.repository.QuestionnaireCommandOutPort;
import com.acme.orderquestionnaire.application.questionnaire.service.context.UpdateQuestionnairePipelineContext;
import com.acme.orderquestionnaire.application.questionnaire.service.step.FetchExistingQuestionnaireStep;
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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@UnitTest
@DisplayName("FetchExistingQuestionnaireStep")
class FetchExistingQuestionnaireStepTest {

    @Test
    @DisplayName("should store questionnaire in context when found")
    void shouldStoreQuestionnaireWhenFound() {
        QuestionnaireCommandOutPort outPort = mock(QuestionnaireCommandOutPort.class);
        Questionnaire questionnaire = mock(Questionnaire.class);
        QuestionnaireId id = QuestionnaireId.of("q_1", "APP", "J_1");
        when(outPort.findQuestionnaireById(id)).thenReturn(Optional.of(questionnaire));

        FetchExistingQuestionnaireStep step = new FetchExistingQuestionnaireStep(outPort);
        UpdateQuestionnairePipelineContext context = new UpdateQuestionnairePipelineContext(
                new UpdateQuestionnaireCommand("q_1", "APP", "J_1", null, null, null, null, null, null));

        Result<Void, List<DomainError>> result = step.execute(context);

        assertInstanceOf(Result.Success.class, result);
        assertSame(questionnaire, context.existingQuestionnaire());
    }

    @Test
    @DisplayName("should fail with QUESTIONNAIRE_NOT_FOUND when questionnaire does not exist")
    void shouldFailWhenQuestionnaireNotFound() {
        QuestionnaireCommandOutPort outPort = mock(QuestionnaireCommandOutPort.class);
        when(outPort.findQuestionnaireById(QuestionnaireId.of("q_missing", "APP", "J_1")))
                .thenReturn(Optional.empty());

        FetchExistingQuestionnaireStep step = new FetchExistingQuestionnaireStep(outPort);
        UpdateQuestionnairePipelineContext context = new UpdateQuestionnairePipelineContext(
                new UpdateQuestionnaireCommand("q_missing", "APP", "J_1", null, null, null, null, null, null));

        Result<Void, List<DomainError>> result = step.execute(context);

        assertInstanceOf(Result.Failure.class, result);
        List<DomainError> errors = result.errorOrElseThrow(() -> new IllegalStateException("Expected failure"));
        assertTrue(errors.stream().anyMatch(e -> e.code().equals("QUESTIONNAIRE_NOT_FOUND")));
    }
}

