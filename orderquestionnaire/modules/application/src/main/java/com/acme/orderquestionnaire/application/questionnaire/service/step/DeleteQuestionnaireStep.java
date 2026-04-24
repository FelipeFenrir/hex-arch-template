package com.acme.orderquestionnaire.application.questionnaire.service.step;

import com.acme.orderquestionnaire.application.questionnaire.port.out.repository.QuestionnaireCommandOutPort;
import com.acme.orderquestionnaire.application.questionnaire.service.context.DeleteQuestionnairePipelineContext;
import com.acme.orderquestionnaire.domain.questionnaire.vo.QuestionnaireId;
import com.acme.shared.pattern.pipeline.RollbackStyle;
import com.acme.shared.pattern.pipeline.Step;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;

import java.util.List;
import java.util.Objects;

/**
 * Performs the physical deletion of the questionnaire identified in the context.
 *
 * <p>{@link RollbackStyle#FRAMEWORK_TRANSACTION} is fixed here because compensating a
 * delete would require re-inserting the original entity with all its data — logic that
 * is not implemented. Rollback is therefore always delegated to the transaction boundary.
 */
public class DeleteQuestionnaireStep implements Step<DeleteQuestionnairePipelineContext> {

    private final QuestionnaireCommandOutPort questionnaireCommandOutPort;

    public DeleteQuestionnaireStep(QuestionnaireCommandOutPort questionnaireCommandOutPort) {
        this.questionnaireCommandOutPort = Objects.requireNonNull(questionnaireCommandOutPort,
                "questionnaireCommandOutPort must not be null");
    }

    @Override
    public String id() {
        return "DELETE_QUESTIONNAIRE";
    }

    @Override
    public RollbackStyle rollbackStyle() {
        return RollbackStyle.FRAMEWORK_TRANSACTION;
    }

    @Override
    public Result<Void, List<DomainError>> execute(DeleteQuestionnairePipelineContext context) {
        var command = context.command();
        QuestionnaireId questionnaireId = QuestionnaireId.of(
                command.id(),
                command.channelDistributionId(),
                command.journeyDistributionId()
        );
        return questionnaireCommandOutPort.deleteById(questionnaireId);
    }
}

