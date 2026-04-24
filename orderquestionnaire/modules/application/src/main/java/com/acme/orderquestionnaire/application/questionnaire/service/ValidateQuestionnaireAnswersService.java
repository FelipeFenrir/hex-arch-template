package com.acme.orderquestionnaire.application.questionnaire.service;

import com.acme.orderquestionnaire.application.questionnaire.dto.command.ValidateQuestionnaireAnswersCommand;
import com.acme.orderquestionnaire.application.questionnaire.dto.view.ValidateQuestionnaireAnswersView;
import com.acme.orderquestionnaire.application.questionnaire.port.in.usecase.ValidateQuestionnaireAnswersUseCase;
import com.acme.orderquestionnaire.application.questionnaire.service.context.ValidateQuestionnaireAnswersPipelineContext;
import com.acme.shared.pattern.pipeline.PipelineOrchestrator;
import com.acme.shared.pattern.pipeline.Step;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;

import java.util.List;

public class ValidateQuestionnaireAnswersService
        extends PipelineOrchestrator<ValidateQuestionnaireAnswersPipelineContext, ValidateQuestionnaireAnswersView>
        implements ValidateQuestionnaireAnswersUseCase {

    public ValidateQuestionnaireAnswersService(List<Step<ValidateQuestionnaireAnswersPipelineContext>> steps) {
        super(steps);
    }

    @Override
    public Result<ValidateQuestionnaireAnswersView, List<DomainError>> execute(ValidateQuestionnaireAnswersCommand command) {
        return run(new ValidateQuestionnaireAnswersPipelineContext(command));
    }

    @Override
    protected ValidateQuestionnaireAnswersView extractResult(ValidateQuestionnaireAnswersPipelineContext context) {
        return context.validationView();
    }
}
