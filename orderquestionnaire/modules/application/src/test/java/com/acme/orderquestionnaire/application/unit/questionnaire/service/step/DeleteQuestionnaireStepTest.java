package com.acme.orderquestionnaire.application.unit.questionnaire.service.step;

import com.acme.orderquestionnaire.application.questionnaire.dto.command.DeleteQuestionnaireCommand;
import com.acme.orderquestionnaire.application.questionnaire.port.out.repository.QuestionnaireCommandOutPort;
import com.acme.orderquestionnaire.application.questionnaire.service.context.DeleteQuestionnairePipelineContext;
import com.acme.orderquestionnaire.application.questionnaire.service.step.DeleteQuestionnaireStep;
import com.acme.orderquestionnaire.domain.questionnaire.vo.QuestionnaireId;
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
@DisplayName("DeleteQuestionnaireStep")
class DeleteQuestionnaireStepTest {

    @Test
    @DisplayName("should use framework transaction rollback style")
    void shouldUseFrameworkTransactionRollbackStyle() {
        DeleteQuestionnaireStep step = new DeleteQuestionnaireStep(mock(QuestionnaireCommandOutPort.class));
        assertEquals(RollbackStyle.FRAMEWORK_TRANSACTION, step.rollbackStyle());
    }

    @Test
    @DisplayName("should delegate deleteById with composite id")
    void shouldDelegateDeleteByIdWithCompositeId() {
        QuestionnaireCommandOutPort outPort = mock(QuestionnaireCommandOutPort.class);
        QuestionnaireId id = QuestionnaireId.of("q_1", "APP", "J_1");
        when(outPort.deleteById(id)).thenReturn(Result.success(null));

        DeleteQuestionnaireStep step = new DeleteQuestionnaireStep(outPort);
        DeleteQuestionnairePipelineContext context = new DeleteQuestionnairePipelineContext(
                new DeleteQuestionnaireCommand("q_1", "APP", "J_1")
        );

        Result<Void, List<DomainError>> result = step.execute(context);

        assertInstanceOf(Result.Success.class, result);
        verify(outPort).deleteById(id);
    }
}

