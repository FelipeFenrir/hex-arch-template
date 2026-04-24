package com.acme.orderquestionnaire.application.question.service.step;

import com.acme.orderquestionnaire.application.question.error.QuestionErrors;
import com.acme.orderquestionnaire.application.question.port.out.repository.QuestionCommandOutPort;
import com.acme.orderquestionnaire.application.question.service.context.DeleteQuestionPipelineContext;
import com.acme.shared.pattern.pipeline.Step;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;

import java.util.List;
import java.util.Objects;

/**
 * Fetches the {@link com.acme.orderquestionnaire.domain.question.Question} to be deleted
 * and stores it in the context for downstream reference-check and deletion steps.
 *
 * <p>Fails fast with {@code QUESTION_NOT_FOUND} when the question does not exist.
 * No side-effects; rollback is a no-op (default {@code NONE}).
 */
public class FetchQuestionForDeleteStep implements Step<DeleteQuestionPipelineContext> {

    private final QuestionCommandOutPort questionCommandOutPort;

    public FetchQuestionForDeleteStep(QuestionCommandOutPort questionCommandOutPort) {
        this.questionCommandOutPort = Objects.requireNonNull(questionCommandOutPort,
                "questionCommandOutPort must not be null");
    }

    @Override
    public String id() {
        return "FETCH_QUESTION_FOR_DELETE";
    }

    @Override
    public Result<Void, List<DomainError>> execute(DeleteQuestionPipelineContext context) {
        return questionCommandOutPort.findQuestionById(context.id())
                .<Result<Void, List<DomainError>>>map(question -> {
                    context.question(question);
                    return Result.success(null);
                })
                .orElseGet(QuestionErrors.QUESTION_NOT_FOUND::asFailure);
    }
}

