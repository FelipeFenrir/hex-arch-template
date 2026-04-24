package com.acme.orderquestionnaire.application.question.service;

import com.acme.orderquestionnaire.application.question.dto.command.UpdateQuestionCommand;
import com.acme.orderquestionnaire.application.question.dto.view.QuestionUpdatedView;
import com.acme.orderquestionnaire.application.question.port.in.usecase.UpdateQuestionUseCase;
import com.acme.orderquestionnaire.application.question.service.context.UpdateQuestionPipelineContext;
import com.acme.shared.pattern.pipeline.PipelineOrchestrator;
import com.acme.shared.pattern.pipeline.Step;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;

import java.util.List;

public class UpdateQuestionService
        extends PipelineOrchestrator<UpdateQuestionPipelineContext, QuestionUpdatedView>
        implements UpdateQuestionUseCase {

    public UpdateQuestionService(List<Step<UpdateQuestionPipelineContext>> steps) {
        super(steps);
    }

    @Override
    public Result<QuestionUpdatedView, List<DomainError>> execute(String id, UpdateQuestionCommand command) {
        return run(new UpdateQuestionPipelineContext(id, command));
    }

    @Override
    protected QuestionUpdatedView extractResult(UpdateQuestionPipelineContext context) {
        return context.updatedView();
    }
}

