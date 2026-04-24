package com.acme.shared.pattern.pipeline;

import com.acme.shared.exception.PipelineFailureException;

/**
 * Defines how a Step handles rollback when the pipeline fails after its execution.
 *
 * <ul>
 *   <li>{@link #NONE} - No side effect that needs undoing (validation, read-only checks). Default.</li>
 *   <li>{@link #COMPENSATION} - The step must undo its own effect; the orchestrator calls
 *       {@link Step#rollback(Object)} in LIFO order (Saga-style compensation).</li>
 *   <li>{@link #FRAMEWORK_TRANSACTION} - Rollback is delegated to the framework (e.g. Spring
 *       {@code @Transactional}). The orchestrator does NOT call {@code rollback()} on these steps;
 *       instead, the boundary layer converts {@code Result.failure} into a
 *       {@link PipelineFailureException} so the transaction manager can react.</li>
 * </ul>
 *
 * <h3>Hybrid scenario</h3>
 * A pipeline can mix modes. For example:
 * <pre>
 *   Step persists to DB          → FRAMEWORK_TRANSACTION  (DB rolls back)
 *   Step publishes an event/call → COMPENSATION           (app rolls back explicitly)
 * </pre>
 * The orchestrator compensates only {@code COMPENSATION} steps; DB rollback is triggered by
 * the framework when {@link PipelineFailureException} propagates past the transactional boundary.
 */
public enum RollbackStyle {
    NONE,
    COMPENSATION,
    FRAMEWORK_TRANSACTION
}

