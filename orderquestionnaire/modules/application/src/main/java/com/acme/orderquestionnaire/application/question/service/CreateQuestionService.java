package com.acme.orderquestionnaire.application.question.service;

import com.acme.orderquestionnaire.application.question.dto.command.CreateQuestionCommand;
import com.acme.orderquestionnaire.application.question.dto.view.QuestionCreatedView;
import com.acme.orderquestionnaire.application.question.port.in.usecase.CreateQuestionUseCase;
import com.acme.orderquestionnaire.application.question.service.context.CreateQuestionPipelineContext;
import com.acme.shared.pattern.pipeline.PipelineOrchestrator;
import com.acme.shared.pattern.pipeline.Step;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;

import com.acme.observability.Loggable;

import java.util.List;

@Loggable
public class CreateQuestionService
        extends PipelineOrchestrator<CreateQuestionPipelineContext, QuestionCreatedView>
        implements CreateQuestionUseCase {

    public CreateQuestionService(List<Step<CreateQuestionPipelineContext>> steps) {
        super(steps);
    }

    @Override
    public Result<QuestionCreatedView, List<DomainError>> execute(CreateQuestionCommand command) {
        return run(new CreateQuestionPipelineContext(command));
    }

    @Override
    protected QuestionCreatedView extractResult(CreateQuestionPipelineContext context) {
        return context.createdView();
    }
}
