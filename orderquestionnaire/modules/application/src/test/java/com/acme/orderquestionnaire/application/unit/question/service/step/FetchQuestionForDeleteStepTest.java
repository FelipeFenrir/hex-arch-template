package com.acme.orderquestionnaire.application.unit.question.service.step;

import com.acme.orderquestionnaire.application.question.port.out.repository.QuestionCommandOutPort;
import com.acme.orderquestionnaire.application.question.service.context.DeleteQuestionPipelineContext;
import com.acme.orderquestionnaire.application.question.service.step.FetchQuestionForDeleteStep;
import com.acme.orderquestionnaire.domain.question.Question;
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
@DisplayName("FetchQuestionForDeleteStep")
class FetchQuestionForDeleteStepTest {

    @Test
    @DisplayName("should fetch and store question when found")
    void shouldFetchAndStoreQuestionWhenFound() {
        QuestionCommandOutPort outPort = mock(QuestionCommandOutPort.class);
        Question question = mock(Question.class);
        when(outPort.findQuestionById("q_1")).thenReturn(Optional.of(question));

        FetchQuestionForDeleteStep step = new FetchQuestionForDeleteStep(outPort);
        DeleteQuestionPipelineContext context = new DeleteQuestionPipelineContext("q_1");

        Result<Void, List<DomainError>> result = step.execute(context);

        assertInstanceOf(Result.Success.class, result);
        assertTrue(context.question() == question);
    }

    @Test
    @DisplayName("should fail when question does not exist")
    void shouldFailWhenQuestionDoesNotExist() {
        QuestionCommandOutPort outPort = mock(QuestionCommandOutPort.class);
        when(outPort.findQuestionById("q_missing")).thenReturn(Optional.empty());

        FetchQuestionForDeleteStep step = new FetchQuestionForDeleteStep(outPort);

        Result<Void, List<DomainError>> result = step.execute(new DeleteQuestionPipelineContext("q_missing"));

        assertInstanceOf(Result.Failure.class, result);
        List<DomainError> errors = result.errorOrElseThrow(() -> new IllegalStateException("Expected failure"));
        assertTrue(errors.stream().anyMatch(error -> error.code().equals("QUESTION_NOT_FOUND")));
    }
}

