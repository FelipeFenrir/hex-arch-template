package com.acme.orderquestionnaire.application.unit.question.service.step;

import com.acme.orderquestionnaire.application.question.port.out.repository.QuestionCommandOutPort;
import com.acme.orderquestionnaire.application.question.service.context.DeleteQuestionPipelineContext;
import com.acme.orderquestionnaire.application.question.service.step.DeleteQuestionStep;
import com.acme.shared.pattern.pipeline.RollbackStyle;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;
import com.acme.shared.stereotypes.test.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@UnitTest
@DisplayName("DeleteQuestionStep")
class DeleteQuestionStepTest {

    @Test
    @DisplayName("should use framework transaction rollback style")
    void shouldUseFrameworkTransactionRollbackStyle() {
        DeleteQuestionStep step = new DeleteQuestionStep(mock(QuestionCommandOutPort.class));
        assertEquals(RollbackStyle.FRAMEWORK_TRANSACTION, step.rollbackStyle());
    }

    @Test
    @DisplayName("should delegate deleteById to out port")
    void shouldDelegateDeleteByIdToOutPort() {
        QuestionCommandOutPort outPort = mock(QuestionCommandOutPort.class);
        when(outPort.deleteById("q_1")).thenReturn(Result.success(null));

        DeleteQuestionStep step = new DeleteQuestionStep(outPort);
        Result<Void, List<DomainError>> result = step.execute(new DeleteQuestionPipelineContext("q_1"));

        assertInstanceOf(Result.Success.class, result);
        verify(outPort).deleteById("q_1");
    }
}

