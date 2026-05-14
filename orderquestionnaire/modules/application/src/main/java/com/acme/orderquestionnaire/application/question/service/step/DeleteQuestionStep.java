package com.acme.orderquestionnaire.application.question.service.step;

import com.acme.orderquestionnaire.application.question.port.out.repository.QuestionCommandOutPort;
import com.acme.orderquestionnaire.application.question.service.context.DeleteQuestionPipelineContext;
import com.acme.shared.pattern.pipeline.RollbackStyle;
import com.acme.shared.pattern.pipeline.Step;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;

import java.util.List;
import java.util.Objects;

/**
 * Performs the physical deletion of the question identified in the context.
 *
 * <p>{@link RollbackStyle#FRAMEWORK_TRANSACTION} is fixed here because compensating a
 * delete would require re-inserting the original entity — logic that is not implemented.
 * Rollback is therefore always delegated to the transaction boundary.
 */
public class DeleteQuestionStep implements Step<DeleteQuestionPipelineContext> {

    private final QuestionCommandOutPort questionCommandOutPort;

    public DeleteQuestionStep(QuestionCommandOutPort questionCommandOutPort) {
        this.questionCommandOutPort = Objects.requireNonNull(questionCommandOutPort,
                "questionCommandOutPort must not be null");
    }

    @Override
    public String id() {
        return "DELETE_QUESTION";
    }

    @Override
    public RollbackStyle rollbackStyle() {
        return RollbackStyle.FRAMEWORK_TRANSACTION;
    }

    @Override
    public Result<Void, List<DomainError>> execute(DeleteQuestionPipelineContext context) {
        return questionCommandOutPort.deleteById(context.questionId().value());
    }
}

