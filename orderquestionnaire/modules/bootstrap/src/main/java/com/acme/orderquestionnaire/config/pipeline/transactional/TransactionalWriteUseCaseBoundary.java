package com.acme.orderquestionnaire.config.pipeline.transactional;

import com.acme.shared.exception.PipelineFailureException;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

abstract class TransactionalWriteUseCaseBoundary {

    private final TransactionTemplate transactionTemplate;

    protected TransactionalWriteUseCaseBoundary(PlatformTransactionManager transactionManager) {
        this.transactionTemplate = transactionManager == null
                ? null
                : new TransactionTemplate(transactionManager);
    }

    protected final <V> Result<V, List<DomainError>> executeInTransaction(
            Supplier<Result<V, List<DomainError>>> useCaseAction) {

        Objects.requireNonNull(useCaseAction, "useCaseAction must not be null");

        if (transactionTemplate == null) {
            Result<V, List<DomainError>> result = useCaseAction.get();
            if (result == null) {
                throw new IllegalStateException("Use case action returned null Result");
            }
            if (result.isFailure()) {
                PipelineFailureException exception = new PipelineFailureException(result.errorOrElseThrow(() ->
                        new IllegalStateException("Expected failure errors in Result")));
                return Result.failure(exception.errors());
            }
            return result;
        }

        try {
            Result<V, List<DomainError>> txResult = transactionTemplate.execute(status -> {
                Result<V, List<DomainError>> result = useCaseAction.get();
                if (result == null) {
                    throw new IllegalStateException("Use case action returned null Result");
                }
                if (result.isFailure()) {
                    throw new PipelineFailureException(result.errorOrElseThrow(() ->
                            new IllegalStateException("Expected failure errors in Result")));
                }
                return result;
            });

            if (txResult == null) {
                throw new IllegalStateException("Transaction callback returned null Result");
            }
            return txResult;
        } catch (PipelineFailureException pipelineFailureException) {
            return Result.failure(pipelineFailureException.errors());
        }
    }
}


