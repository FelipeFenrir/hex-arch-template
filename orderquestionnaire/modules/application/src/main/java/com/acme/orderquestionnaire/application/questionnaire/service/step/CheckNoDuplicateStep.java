package com.acme.orderquestionnaire.application.questionnaire.service.step;

import com.acme.orderquestionnaire.application.questionnaire.error.QuestionnaireErrors;
import com.acme.orderquestionnaire.application.questionnaire.port.out.repository.QuestionnaireCommandOutPort;
import com.acme.orderquestionnaire.application.questionnaire.service.context.CreateQuestionnairePipelineContext;
import com.acme.orderquestionnaire.domain.questionnaire.vo.QuestionnaireId;
import com.acme.shared.pattern.pipeline.Step;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;

import java.util.List;
import java.util.Objects;

/**
 * Guards against creating a duplicate questionnaire for the same composite identity
 * ({@code id + channelDistributionId + journeyDistributionId}).
 *
 * <p>Read-only operation; no side-effects. Rollback is a no-op (default {@code NONE}).
 */
public class CheckNoDuplicateStep implements Step<CreateQuestionnairePipelineContext> {

    private final QuestionnaireCommandOutPort questionnaireCommandOutPort;

    public CheckNoDuplicateStep(QuestionnaireCommandOutPort questionnaireCommandOutPort) {
        this.questionnaireCommandOutPort = Objects.requireNonNull(questionnaireCommandOutPort,
                "questionnaireCommandOutPort must not be null");
    }

    @Override
    public String id() {
        return "CHECK_NO_DUPLICATE";
    }

    @Override
    public Result<Void, List<DomainError>> execute(CreateQuestionnairePipelineContext context) {
        var command = context.command();
        var questionnaireId = QuestionnaireId.of(
                command.id(),
                command.channelDistributionId(),
                command.journeyDistributionId()
        );

        return questionnaireCommandOutPort.existsById(questionnaireId)
                ? QuestionnaireErrors.QUESTIONNAIRE_ALREADY_EXISTS.asFailure(command.id())
                : Result.success(null);
    }
}

