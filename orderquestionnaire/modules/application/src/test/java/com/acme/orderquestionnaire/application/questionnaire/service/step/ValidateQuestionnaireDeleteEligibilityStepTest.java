package com.acme.orderquestionnaire.application.questionnaire.service.step;

import com.acme.orderquestionnaire.application.questionnaire.dto.command.DeleteQuestionnaireCommand;
import com.acme.orderquestionnaire.application.questionnaire.service.context.DeleteQuestionnairePipelineContext;
import com.acme.orderquestionnaire.domain.questionnaire.Questionnaire;
import com.acme.shared.enumerator.ParameterizationStatus;
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
@DisplayName("ValidateQuestionnaireDeleteEligibilityStep")
class ValidateQuestionnaireDeleteEligibilityStepTest {

    private final ValidateQuestionnaireDeleteEligibilityStep step = new ValidateQuestionnaireDeleteEligibilityStep();

    @Test
    @DisplayName("should succeed when questionnaire can be deleted")
    void shouldSucceedWhenQuestionnaireCanBeDeleted() {
        Questionnaire questionnaire = mock(Questionnaire.class);
        when(questionnaire.canBeDeleted()).thenReturn(true);

        DeleteQuestionnairePipelineContext context = new DeleteQuestionnairePipelineContext(
                new DeleteQuestionnaireCommand("q_1", "APP", "J_1")
        );
        context.questionnaire(questionnaire);

        Result<Void, List<DomainError>> result = step.execute(context);

        assertInstanceOf(Result.Success.class, result);
    }

    @Test
    @DisplayName("should fail when questionnaire cannot be deleted")
    void shouldFailWhenQuestionnaireCannotBeDeleted() {
        Questionnaire questionnaire = mock(Questionnaire.class);
        when(questionnaire.canBeDeleted()).thenReturn(false);
        when(questionnaire.status()).thenReturn(ParameterizationStatus.ACTIVE);

        DeleteQuestionnairePipelineContext context = new DeleteQuestionnairePipelineContext(
                new DeleteQuestionnaireCommand("q_1", "APP", "J_1")
        );
        context.questionnaire(questionnaire);

        Result<Void, List<DomainError>> result = step.execute(context);

        assertInstanceOf(Result.Failure.class, result);
        List<DomainError> errors = result.errorOrElseThrow(() -> new IllegalStateException("Expected failure"));
        assertTrue(errors.stream().anyMatch(error -> error.code().equals("QUESTIONNAIRE_DELETE_NOT_ALLOWED")));
    }
}

