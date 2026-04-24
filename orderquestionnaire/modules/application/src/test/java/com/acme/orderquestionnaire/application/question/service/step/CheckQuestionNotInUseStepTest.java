package com.acme.orderquestionnaire.application.question.service.step;

import com.acme.orderquestionnaire.application.question.service.context.DeleteQuestionPipelineContext;
import com.acme.orderquestionnaire.application.questionnaire.port.out.repository.QuestionnaireCommandOutPort;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;
import com.acme.shared.stereotypes.test.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@UnitTest
@DisplayName("CheckQuestionNotInUseStep")
class CheckQuestionNotInUseStepTest {

    @Test
    @DisplayName("should succeed when question has no references")
    void shouldSucceedWhenQuestionHasNoReferences() {
        QuestionnaireCommandOutPort outPort = mock(QuestionnaireCommandOutPort.class);
        when(outPort.findReferencingQuestionnaireIdsByQuestionIds(List.of("q_1"))).thenReturn(Map.of());

        CheckQuestionNotInUseStep step = new CheckQuestionNotInUseStep(outPort);
        Result<Void, List<DomainError>> result = step.execute(new DeleteQuestionPipelineContext("q_1"));

        assertInstanceOf(Result.Success.class, result);
    }

    @Test
    @DisplayName("should fail and store sorted related ids when question is referenced")
    void shouldFailAndStoreSortedRelatedIdsWhenQuestionIsReferenced() {
        QuestionnaireCommandOutPort outPort = mock(QuestionnaireCommandOutPort.class);
        when(outPort.findReferencingQuestionnaireIdsByQuestionIds(List.of("q_2")))
                .thenReturn(Map.of("q_2", List.of("qst_z", "qst_a", "qst_a")));

        CheckQuestionNotInUseStep step = new CheckQuestionNotInUseStep(outPort);
        DeleteQuestionPipelineContext context = new DeleteQuestionPipelineContext("q_2");

        Result<Void, List<DomainError>> result = step.execute(context);

        assertInstanceOf(Result.Failure.class, result);
        List<DomainError> errors = result.errorOrElseThrow(() -> new IllegalStateException("Expected failure"));
        assertTrue(errors.stream().anyMatch(error -> error.code().equals("QUESTION_IN_USE")));
        assertEquals(List.of("qst_a", "qst_z"), context.relatedQuestionnaireIds());
    }
}


