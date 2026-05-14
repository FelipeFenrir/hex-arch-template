package com.acme.orderquestionnaire.application.questionnaire.service;

import com.acme.orderquestionnaire.application.questionnaire.dto.command.CreateQuestionnaireCommand;
import com.acme.orderquestionnaire.application.questionnaire.dto.view.QuestionnaireCreatedView;
import com.acme.orderquestionnaire.application.questionnaire.port.in.usecase.CreateQuestionnaireUseCase;
import com.acme.orderquestionnaire.application.questionnaire.service.context.CreateQuestionnairePipelineContext;
import com.acme.shared.pattern.pipeline.PipelineContext;
import com.acme.shared.pattern.pipeline.PipelineOrchestrator;
import com.acme.shared.pattern.pipeline.Step;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;

import com.acme.observability.Loggable;

import java.util.List;

/**
 * Orchestrates the creation of a {@link com.acme.orderquestionnaire.domain.questionnaire.Questionnaire}
 * through an ordered pipeline of independent {@link Step}s.
 *
 * <p>Construction responsibilities:
 * <ul>
 *   <li>Receives a {@code List<Step>} that is already ordered and filtered by the
 *       bootstrap configuration ({@code CreateQuestionnaireUseCaseConfig}).</li>
 *   <li>Delegates execution, short-circuit and LIFO compensation rollback entirely to
 *       {@link PipelineOrchestrator#run(PipelineContext)}.</li>
 * </ul>
 *
 * <p>Default pipeline steps (in order, as declared in {@code application.yml}):
 * <ol>
 *   <li>{@code VALIDATE_COMMAND} — field-level accumulation of validation errors</li>
 *   <li>{@code BUILD_AUDIT} — constructs and validates {@code AuditInfo}</li>
 *   <li>{@code CHECK_CHANNEL_DISTRIBUTION} — ensures the channel exists</li>
 *   <li>{@code CHECK_JOURNEY_DISTRIBUTION} — ensures the journey exists</li>
 *   <li>{@code CHECK_NO_DUPLICATE} — guards against duplicate questionnaire identity</li>
 *   <li>{@code BUILD_AND_PERSIST_QUESTIONNAIRE} — builds domain entity and persists it</li>
 * </ol>
 */
@Loggable
public class CreateQuestionnaireService
        extends PipelineOrchestrator<CreateQuestionnairePipelineContext, QuestionnaireCreatedView>
        implements CreateQuestionnaireUseCase {

    public CreateQuestionnaireService(List<Step<CreateQuestionnairePipelineContext>> steps) {
        super(steps);
    }

    @Override
    public Result<QuestionnaireCreatedView, List<DomainError>> execute(CreateQuestionnaireCommand command) {
        return run(new CreateQuestionnairePipelineContext(command));
    }

    @Override
    protected QuestionnaireCreatedView extractResult(CreateQuestionnairePipelineContext context) {
        return context.createdView();
    }
}
