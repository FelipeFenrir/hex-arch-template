package com.acme.orderquestionnaire.application.questionnaire.service;

import com.acme.orderquestionnaire.application.questionnaire.dto.command.UpdateQuestionnaireCommand;
import com.acme.orderquestionnaire.application.questionnaire.dto.view.QuestionnaireUpdatedView;
import com.acme.orderquestionnaire.application.questionnaire.port.in.usecase.UpdateQuestionnaireUseCase;
import com.acme.orderquestionnaire.application.questionnaire.service.context.UpdateQuestionnairePipelineContext;
import com.acme.shared.pattern.pipeline.PipelineOrchestrator;
import com.acme.shared.pattern.pipeline.Step;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;

import java.util.List;

public class UpdateQuestionnaireService
        extends PipelineOrchestrator<UpdateQuestionnairePipelineContext, QuestionnaireUpdatedView>
        implements UpdateQuestionnaireUseCase {

    public UpdateQuestionnaireService(List<Step<UpdateQuestionnairePipelineContext>> steps) {
        super(steps);
    }

    @Override
    public Result<QuestionnaireUpdatedView, List<DomainError>> execute(UpdateQuestionnaireCommand command) {
        return run(new UpdateQuestionnairePipelineContext(command));
    }

    @Override
    protected QuestionnaireUpdatedView extractResult(UpdateQuestionnairePipelineContext context) {
        return context.updatedView();
    }
}
