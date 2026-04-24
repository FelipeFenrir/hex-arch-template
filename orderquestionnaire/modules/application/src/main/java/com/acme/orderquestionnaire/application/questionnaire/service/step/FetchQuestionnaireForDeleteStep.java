package com.acme.orderquestionnaire.application.questionnaire.service.step;

import com.acme.orderquestionnaire.application.questionnaire.error.QuestionnaireErrors;
import com.acme.orderquestionnaire.application.questionnaire.port.out.repository.QuestionnaireCommandOutPort;
import com.acme.orderquestionnaire.application.questionnaire.service.context.DeleteQuestionnairePipelineContext;
import com.acme.orderquestionnaire.domain.questionnaire.vo.QuestionnaireId;
import com.acme.shared.pattern.pipeline.Step;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;

import java.util.List;
import java.util.Objects;

/**
 * Fetches the {@link com.acme.orderquestionnaire.domain.questionnaire.Questionnaire} to be deleted
 * and stores it in the context for downstream eligibility validation and deletion steps.
 *
 * <p>Fails fast with {@code QUESTIONNAIRE_NOT_FOUND} when the questionnaire does not exist.
 * No side-effects; rollback is a no-op (default {@code NONE}).
 */
public class FetchQuestionnaireForDeleteStep implements Step<DeleteQuestionnairePipelineContext> {

    private final QuestionnaireCommandOutPort questionnaireCommandOutPort;

    public FetchQuestionnaireForDeleteStep(QuestionnaireCommandOutPort questionnaireCommandOutPort) {
        this.questionnaireCommandOutPort = Objects.requireNonNull(questionnaireCommandOutPort,
                "questionnaireCommandOutPort must not be null");
    }

    @Override
    public String id() {
        return "FETCH_QUESTIONNAIRE_FOR_DELETE";
    }

    @Override
    public Result<Void, List<DomainError>> execute(DeleteQuestionnairePipelineContext context) {
        var command = context.command();
        QuestionnaireId questionnaireId = QuestionnaireId.of(
                command.id(),
                command.channelDistributionId(),
                command.journeyDistributionId()
        );

        return questionnaireCommandOutPort.findQuestionnaireById(questionnaireId)
                .<Result<Void, List<DomainError>>>map(questionnaire -> {
                    context.questionnaire(questionnaire);
                    return Result.success(null);
                })
                .orElseGet(() -> QuestionnaireErrors.QUESTIONNAIRE_NOT_FOUND.asFailure(command.id()));
    }
}

