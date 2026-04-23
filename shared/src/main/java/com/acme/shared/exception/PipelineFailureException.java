package com.acme.shared.exception;

import com.acme.shared.pattern.result.DomainError;

import java.util.List;
import java.util.Objects;

/**
 * Signals that a pipeline execution failed and carries the accumulated list of
 * {@link DomainError}s from the failing step.
 *
 * <p>This exception is used at the <strong>transactional boundary</strong> (e.g. an entry-point
 * service annotated with {@code @Transactional}) to force the framework to roll back the
 * database transaction, while still preserving the structured error list for the caller.
 *
 * <p>Usage pattern at the infrastructure boundary:
 * <pre>{@code
 * @Transactional
 * public QuestionnaireCreatedView execute(CreateQuestionnaireCommand command) {
 *     return createQuestionnaireUseCase.execute(command)
 *             .getOrElseThrow(PipelineFailureException::new);
 * }
 * }</pre>
 *
 * <p>The adapter (REST controller, gRPC handler, queue consumer) then catches
 * {@link PipelineFailureException} and maps {@link #errors()} back to the
 * appropriate protocol error response — without rethrowing or losing the error list.
 *
 * @see com.acme.shared.engine.pipeline.PipelineOrchestrator
 * @see com.acme.shared.engine.pipeline.RollbackStyle#FRAMEWORK_TRANSACTION
 */
public class PipelineFailureException extends RuntimeException {

    private final List<DomainError> errors;

    public PipelineFailureException(List<DomainError> errors) {
        super("Pipeline failed: %d error(s) reported".formatted(
            errors == null ? 0 : errors.size()
        ));
        this.errors = List.copyOf(Objects.requireNonNull(errors, "errors must not be null"));
    }

    /**
     * The structured domain errors that caused the pipeline to fail.
     * Never null, never empty.
     */
    public List<DomainError> errors() {
        return errors;
    }
}

