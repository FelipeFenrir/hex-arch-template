package com.acme.orderquestionnaire.application.question.service;

import com.acme.orderquestionnaire.application.question.dto.view.DeleteQuestionFailureView;
import com.acme.orderquestionnaire.application.question.dto.view.DeleteQuestionsResultView;
import com.acme.orderquestionnaire.application.question.error.QuestionErrors;
import com.acme.orderquestionnaire.application.question.port.in.usecase.DeleteQuestionUseCase;
import com.acme.orderquestionnaire.application.question.service.context.DeleteQuestionPipelineContext;
import com.acme.shared.pattern.pipeline.PipelineOrchestrator;
import com.acme.shared.pattern.pipeline.Step;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;

import java.util.ArrayList;
import java.util.List;

/**
 * Orchestrates the deletion of a {@link com.acme.orderquestionnaire.domain.question.Question}
 * through an ordered pipeline of independent {@link Step}s.
 *
 * <p>Default pipeline steps (in order, as declared in {@code application.yml}):
 * <ol>
 *   <li>{@code VALIDATE_QUESTION_ID} — validates the question ID is non-blank</li>
 *   <li>{@code FETCH_QUESTION_FOR_DELETE} — loads the question or fails with NOT_FOUND</li>
 *   <li>{@code CHECK_NOT_IN_USE} — fails with QUESTION_IN_USE if any questionnaire references this question</li>
 *   <li>{@code DELETE_QUESTION} — performs the physical deletion</li>
 * </ol>
 *
 * <p>The batch overload {@link #execute(List)} runs the single-delete pipeline per ID,
 * collecting per-item failures. A global failure is only returned when the input list is null or empty.
 */
public class DeleteQuestionService
        extends PipelineOrchestrator<DeleteQuestionPipelineContext, Void>
        implements DeleteQuestionUseCase {

    public DeleteQuestionService(List<Step<DeleteQuestionPipelineContext>> steps) {
        super(steps);
    }

    @Override
    public Result<Void, List<DomainError>> execute(String id) {
        return run(new DeleteQuestionPipelineContext(id));
    }

    @Override
    public Result<DeleteQuestionsResultView, List<DomainError>> execute(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return QuestionErrors.INVALID_IDS.asFailure();
        }

        List<DeleteQuestionFailureView> failures = new ArrayList<>();

        for (String id : ids) {
            DeleteQuestionPipelineContext context = new DeleteQuestionPipelineContext(id);
            Result<Void, List<DomainError>> result = run(context);
            if (result.isFailure()) {
                List<DomainError> errors = result.errorOrElseThrow(() ->
                        new IllegalStateException("Expected failure result"));
                DomainError first = errors.isEmpty()
                        ? QuestionErrors.QUESTION_DELETE_FAILED.toDomainError(id, "unknown reason")
                        : errors.getFirst();
                failures.add(new DeleteQuestionFailureView(
                        id, first.code(), first.message(), context.relatedQuestionnaireIds()
                ));
            }
        }

        return Result.success(new DeleteQuestionsResultView(failures));
    }

    @Override
    protected Void extractResult(DeleteQuestionPipelineContext context) {
        return null;
    }
}

