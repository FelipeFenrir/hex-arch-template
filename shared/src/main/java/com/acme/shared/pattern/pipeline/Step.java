package com.acme.shared.pattern.pipeline;

import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;

import java.util.List;

/**
 * A single, independent unit of work within a {@link PipelineOrchestrator}.
 *
 * <p>Contract rules:
 * <ul>
 *   <li>Steps must <strong>not</strong> know about each other — the only communication channel
 *       between steps is the shared {@link PipelineContext} (Data Bag).</li>
 *   <li>{@link #execute(Object)} must return {@code Result.failure} instead of throwing for
 *       expected business failures. Technical exceptions are caught by the orchestrator and
 *       converted to {@code Result.failure} automatically.</li>
 *   <li>{@link #rollback(Object)} is invoked only when the step previously succeeded
 *       and {@link #rollbackStyle()} is {@link RollbackStyle#COMPENSATION}.
 *       Implementations must be idempotent and must not throw.</li>
 * </ul>
 *
 * @param <C> the pipeline context type (must extend {@link PipelineContext})
 */
public interface Step<C> {

    /**
     * Unique identifier for this step.
     * Must match the key used in the external configuration (e.g. YAML) to allow
     * ordered and conditional activation from outside the application layer.
     */
    String id();

    /**
     * Executes the step logic against the given context.
     *
     * @param context shared data bag; read inputs and write outputs here
     * @return {@code Result.success(null)} on success, or
     *         {@code Result.failure(errors)} on business failure
     */
    Result<Void, List<DomainError>> execute(C context);

    /**
     * The rollback strategy for this step. Defaults to {@link RollbackStyle#NONE}.
     * Override to declare that this step requires compensating actions or framework rollback.
     */
    default RollbackStyle rollbackStyle() {
        return RollbackStyle.NONE;
    }

    /**
     * Undoes the side effect of a previous successful {@link #execute(Object)}.
     * Called by the orchestrator in LIFO order only when:
     * <ul>
     *   <li>this step's {@link #rollbackStyle()} is {@link RollbackStyle#COMPENSATION}, and</li>
     *   <li>a later step in the pipeline failed.</li>
     * </ul>
     * <p>Default is a no-op — steps without observable side-effects do not need to override.</p>
     *
     * @param context the same context that was passed to {@link #execute(Object)}
     */
    default void rollback(C context) {
        // no-op by default
    }
}

