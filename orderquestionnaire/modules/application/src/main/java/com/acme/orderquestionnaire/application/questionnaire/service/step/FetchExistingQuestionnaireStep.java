package com.acme.orderquestionnaire.application.questionnaire.service.step;

import com.acme.orderquestionnaire.application.questionnaire.error.QuestionnaireErrors;
import com.acme.orderquestionnaire.application.questionnaire.port.out.repository.QuestionnaireCommandOutPort;
import com.acme.orderquestionnaire.application.questionnaire.service.context.UpdateQuestionnairePipelineContext;
import com.acme.orderquestionnaire.domain.questionnaire.vo.QuestionnaireId;
import com.acme.shared.pattern.pipeline.Step;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;

import java.util.List;
import java.util.Objects;

public class FetchExistingQuestionnaireStep implements Step<UpdateQuestionnairePipelineContext> {

    private final QuestionnaireCommandOutPort questionnaireCommandOutPort;

    public FetchExistingQuestionnaireStep(QuestionnaireCommandOutPort questionnaireCommandOutPort) {
        this.questionnaireCommandOutPort = Objects.requireNonNull(questionnaireCommandOutPort,
                "questionnaireCommandOutPort must not be null");
    }

    @Override
    public String id() {
        return "FETCH_EXISTING_QUESTIONNAIRE";
    }

    @Override
    public Result<Void, List<DomainError>> execute(UpdateQuestionnairePipelineContext context) {
        var command = context.command();
        QuestionnaireId questionnaireId = QuestionnaireId.of(
                command.id(),
                command.channelDistributionId(),
                command.journeyDistributionId()
        );

        return questionnaireCommandOutPort.findQuestionnaireById(questionnaireId)
                .<Result<Void, List<DomainError>>>map(questionnaire -> {
                    context.existingQuestionnaire(questionnaire);
                    return Result.success(null);
                })
                .orElseGet(() -> QuestionnaireErrors.QUESTIONNAIRE_NOT_FOUND.asFailure(command.id()));
    }
}

