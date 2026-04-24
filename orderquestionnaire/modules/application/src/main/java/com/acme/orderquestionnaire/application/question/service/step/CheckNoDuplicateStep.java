package com.acme.orderquestionnaire.application.question.service.step;

import com.acme.orderquestionnaire.application.question.error.QuestionErrors;
import com.acme.orderquestionnaire.application.question.port.out.repository.QuestionCommandOutPort;
import com.acme.orderquestionnaire.application.question.service.context.CreateQuestionPipelineContext;
import com.acme.shared.pattern.pipeline.Step;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;

import java.util.List;
import java.util.Objects;

public class CheckNoDuplicateStep implements Step<CreateQuestionPipelineContext> {

    private final QuestionCommandOutPort questionCommandOutPort;

    public CheckNoDuplicateStep(QuestionCommandOutPort questionCommandOutPort) {
        this.questionCommandOutPort = Objects.requireNonNull(questionCommandOutPort,
                "questionCommandOutPort must not be null");
    }

    @Override
    public String id() {
        return "CHECK_NO_DUPLICATE";
    }

    @Override
    public Result<Void, List<DomainError>> execute(CreateQuestionPipelineContext context) {
        String id = context.command().id();
        return questionCommandOutPort.existsById(id)
                ? QuestionErrors.QUESTION_ALREADY_EXISTS.asFailure(id)
                : Result.success(null);
    }
}

