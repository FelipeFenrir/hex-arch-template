package com.acme.orderquestionnaire.application.question.service.step;

import com.acme.orderquestionnaire.application.question.error.QuestionErrors;
import com.acme.orderquestionnaire.application.question.port.out.repository.QuestionCommandOutPort;
import com.acme.orderquestionnaire.application.question.service.context.UpdateQuestionPipelineContext;
import com.acme.shared.pattern.pipeline.Step;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;

import java.util.List;
import java.util.Objects;

public class FetchExistingQuestionStep implements Step<UpdateQuestionPipelineContext> {

    private final QuestionCommandOutPort questionCommandOutPort;

    public FetchExistingQuestionStep(QuestionCommandOutPort questionCommandOutPort) {
        this.questionCommandOutPort = Objects.requireNonNull(questionCommandOutPort,
                "questionCommandOutPort must not be null");
    }

    @Override
    public String id() {
        return "FETCH_EXISTING_QUESTION";
    }

    @Override
    public Result<Void, List<DomainError>> execute(UpdateQuestionPipelineContext context) {
        return questionCommandOutPort.findQuestionById(context.questionId().value())
                .<Result<Void, List<DomainError>>>map(question -> {
                    context.existingQuestion(question);
                    return Result.success(null);
                })
                .orElseGet(QuestionErrors.QUESTION_NOT_FOUND::asFailure);
    }
}

