package com.acme.orderquestionnaire.application.questionnaire.service;

import com.acme.orderquestionnaire.application.questionnaire.dto.command.DeleteQuestionnaireCommand;
import com.acme.orderquestionnaire.application.questionnaire.dto.view.DeleteQuestionnaireFailureView;
import com.acme.orderquestionnaire.application.questionnaire.dto.view.DeleteQuestionnairesResultView;
import com.acme.orderquestionnaire.application.questionnaire.error.QuestionnaireErrors;
import com.acme.orderquestionnaire.application.questionnaire.port.in.usecase.DeleteQuestionnaireUseCase;
import com.acme.orderquestionnaire.application.questionnaire.service.context.DeleteQuestionnairePipelineContext;
import com.acme.shared.pattern.pipeline.PipelineOrchestrator;
import com.acme.shared.pattern.pipeline.Step;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;
import com.acme.observability.Loggable;

import java.util.ArrayList;
import java.util.List;

/**
 * Orchestrates the deletion of a {@link com.acme.orderquestionnaire.domain.questionnaire.Questionnaire}
 * through an ordered pipeline of independent {@link Step}s.
 *
 * <p>Default pipeline steps (in order, as declared in {@code application.yml}):
 * <ol>
 *   <li>{@code VALIDATE_DELETE_COMMAND} — validates composite identity fields</li>
 *   <li>{@code FETCH_QUESTIONNAIRE_FOR_DELETE} — loads the questionnaire or fails with NOT_FOUND</li>
 *   <li>{@code VALIDATE_DELETE_ELIGIBILITY} — ensures status is DRAFT or INACTIVE</li>
 *   <li>{@code DELETE_QUESTIONNAIRE} — performs the physical deletion</li>
 * </ol>
 *
 * <p>The batch overload {@link #execute(List)} runs the single-delete pipeline per command,
 * collecting per-item failures. A global failure is only returned when the input list is null or empty.
 */
@Loggable
public class DeleteQuestionnaireService
        extends PipelineOrchestrator<DeleteQuestionnairePipelineContext, Void>
        implements DeleteQuestionnaireUseCase {

    public DeleteQuestionnaireService(List<Step<DeleteQuestionnairePipelineContext>> steps) {
        super(steps);
    }

    @Override
    public Result<Void, List<DomainError>> execute(DeleteQuestionnaireCommand command) {
        return run(new DeleteQuestionnairePipelineContext(command));
    }

    @Override
    public Result<DeleteQuestionnairesResultView, List<DomainError>> execute(List<DeleteQuestionnaireCommand> commands) {
        if (commands == null || commands.isEmpty()) {
            return QuestionnaireErrors.INVALID_IDS.asFailure();
        }

        List<DeleteQuestionnaireFailureView> failures = new ArrayList<>();

        for (DeleteQuestionnaireCommand command : commands) {
            Result<Void, List<DomainError>> result = execute(command);
            if (result.isFailure()) {
                List<DomainError> errors = result.errorOrElseThrow(() ->
                        new IllegalStateException("Expected failure result"));
                DomainError first = errors.isEmpty()
                        ? QuestionnaireErrors.QUESTIONNAIRE_DELETE_FAILED.toDomainError(
                                command != null ? command.id() : null, "unknown reason")
                        : errors.getFirst();
                failures.add(new DeleteQuestionnaireFailureView(
                        command != null ? command.id() : null,
                        command != null ? command.channelDistributionId() : null,
                        command != null ? command.journeyDistributionId() : null,
                        first.code(),
                        first.message()
                ));
            }
        }

        return Result.success(new DeleteQuestionnairesResultView(failures));
    }

    @Override
    protected Void extractResult(DeleteQuestionnairePipelineContext context) {
        return null;
    }
}
