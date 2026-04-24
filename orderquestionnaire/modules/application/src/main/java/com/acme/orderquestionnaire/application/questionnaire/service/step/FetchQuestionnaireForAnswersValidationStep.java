package com.acme.orderquestionnaire.application.questionnaire.service.step;

import com.acme.orderquestionnaire.application.questionnaire.error.QuestionnaireErrors;
import com.acme.orderquestionnaire.application.questionnaire.port.out.repository.QuestionnaireCommandOutPort;
import com.acme.orderquestionnaire.application.questionnaire.service.context.ValidateQuestionnaireAnswersPipelineContext;
import com.acme.orderquestionnaire.domain.questionnaire.vo.QuestionnaireId;
import com.acme.shared.pattern.pipeline.Step;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;

import java.util.List;
import java.util.Objects;

/**
 * Resolves questionnaire by composite identity and stores it in the pipeline context.
 */
public class FetchQuestionnaireForAnswersValidationStep implements Step<ValidateQuestionnaireAnswersPipelineContext> {

    private final QuestionnaireCommandOutPort questionnaireCommandOutPort;

    public FetchQuestionnaireForAnswersValidationStep(QuestionnaireCommandOutPort questionnaireCommandOutPort) {
        this.questionnaireCommandOutPort = Objects.requireNonNull(questionnaireCommandOutPort,
                "questionnaireCommandOutPort must not be null");
    }

    @Override
    public String id() {
        return "FETCH_QUESTIONNAIRE_FOR_ANSWER_VALIDATION";
    }

    @Override
    public Result<Void, List<DomainError>> execute(ValidateQuestionnaireAnswersPipelineContext context) {
        var command = context.command();
        QuestionnaireId id = QuestionnaireId.of(
                command.questionnaireId(),
                command.channelDistributionId(),
                command.journeyDistributionId()
        );

        return questionnaireCommandOutPort.findQuestionnaireById(id)
                .<Result<Void, List<DomainError>>>map(questionnaire -> {
                    context.questionnaire(questionnaire);
                    return Result.success(null);
                })
                .orElseGet(() -> QuestionnaireErrors.QUESTIONNAIRE_NOT_FOUND.asFailure(command.questionnaireId()));
    }
}

