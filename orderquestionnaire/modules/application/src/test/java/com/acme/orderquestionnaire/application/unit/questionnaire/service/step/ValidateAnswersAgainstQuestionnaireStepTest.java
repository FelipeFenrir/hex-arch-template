package com.acme.orderquestionnaire.application.unit.questionnaire.service.step;

import com.acme.orderquestionnaire.application.questionnaire.dto.command.ValidateQuestionnaireAnswersCommand;
import com.acme.orderquestionnaire.application.questionnaire.service.context.ValidateQuestionnaireAnswersPipelineContext;
import com.acme.orderquestionnaire.application.questionnaire.service.step.ValidateAnswersAgainstQuestionnaireStep;
import com.acme.orderquestionnaire.domain.questionnaire.QuestionValidationFailure;
import com.acme.orderquestionnaire.domain.questionnaire.Questionnaire;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;
import com.acme.shared.stereotypes.test.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@UnitTest
@DisplayName("ValidateAnswersAgainstQuestionnaireStep")
class ValidateAnswersAgainstQuestionnaireStepTest {

    private final ValidateAnswersAgainstQuestionnaireStep step = new ValidateAnswersAgainstQuestionnaireStep();

    @Test
    @DisplayName("should produce valid=true view when answer validation succeeds")
    void shouldProduceValidViewWhenAnswerValidationSucceeds() {
        Questionnaire questionnaire = mock(Questionnaire.class);
        when(questionnaire.id()).thenReturn("q_1");
        when(questionnaire.channelDistributionId()).thenReturn("APP");
        when(questionnaire.journeyDistributionId()).thenReturn("J_1");
        when(questionnaire.answerValidation(Map.of("k", "v"))).thenReturn(Result.success(null));

        ValidateQuestionnaireAnswersPipelineContext context = contextWith(questionnaire, Map.of("k", "v"));

        Result<Void, List<DomainError>> result = step.execute(context);

        assertInstanceOf(Result.Success.class, result);
        assertTrue(context.validationView().valid());
        assertTrue(context.validationView().violationsByQuestionId().isEmpty());
    }

    @Test
    @DisplayName("should map failures to validation view when answer validation fails")
    void shouldMapFailuresToValidationViewWhenAnswerValidationFails() {
        Questionnaire questionnaire = mock(Questionnaire.class);
        when(questionnaire.id()).thenReturn("q_1");
        when(questionnaire.channelDistributionId()).thenReturn("APP");
        when(questionnaire.journeyDistributionId()).thenReturn("J_1");
        when(questionnaire.configuredQuestions()).thenReturn(List.of());
        when(questionnaire.answerValidation(Map.of("q_income", -10))).thenReturn(Result.failure(List.of(
                new QuestionValidationFailure(
                        "q_income",
                        "Income",
                        1,
                        List.of(new DomainError("VALUE_BELOW_MIN", "below min"))
                )
        )));

        ValidateQuestionnaireAnswersPipelineContext context = contextWith(questionnaire, Map.of("q_income", -10));

        Result<Void, List<DomainError>> result = step.execute(context);

        assertInstanceOf(Result.Success.class, result);
        assertFalse(context.validationView().valid());
        assertTrue(context.validationView().violationsByQuestionId().containsKey("q_income"));
        assertEquals(-10, context.validationView().violationsByQuestionId().get("q_income").providedAnswer());
    }

    private static ValidateQuestionnaireAnswersPipelineContext contextWith(Questionnaire questionnaire,
                                                                           Map<String, Object> answers) {
        ValidateQuestionnaireAnswersPipelineContext context = new ValidateQuestionnaireAnswersPipelineContext(
                new ValidateQuestionnaireAnswersCommand("q_1", "APP", "J_1", answers)
        );
        context.questionnaire(questionnaire);
        return context;
    }
}

