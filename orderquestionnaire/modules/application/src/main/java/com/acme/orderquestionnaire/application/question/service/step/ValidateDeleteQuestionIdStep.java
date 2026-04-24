package com.acme.orderquestionnaire.application.question.service.step;

import com.acme.orderquestionnaire.application.question.error.QuestionErrors;
import com.acme.orderquestionnaire.application.question.service.context.DeleteQuestionPipelineContext;
import com.acme.shared.pattern.pipeline.Step;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Guard;
import com.acme.shared.pattern.result.Result;

import java.util.List;

/**
 * Validates that the question ID supplied to the delete pipeline is non-blank.
 *
 * <p>No side-effects; rollback is a no-op (default {@code NONE}).
 */
public class ValidateDeleteQuestionIdStep implements Step<DeleteQuestionPipelineContext> {

    @Override
    public String id() {
        return "VALIDATE_QUESTION_ID";
    }

    @Override
    public Result<Void, List<DomainError>> execute(DeleteQuestionPipelineContext context) {
        return Guard.requireNonBlank(context.id(), QuestionErrors.INVALID_ID);
    }
}

