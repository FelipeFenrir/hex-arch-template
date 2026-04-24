package com.acme.orderquestionnaire.config.pipeline.transactional;

import com.acme.orderquestionnaire.application.question.dto.view.DeleteQuestionsResultView;
import com.acme.orderquestionnaire.application.question.port.in.usecase.DeleteQuestionUseCase;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;
import java.util.Objects;

public class TransactionalDeleteQuestionUseCaseFacade extends TransactionalWriteUseCaseBoundary
        implements DeleteQuestionUseCase {

    private final DeleteQuestionUseCase delegate;

    public TransactionalDeleteQuestionUseCaseFacade(PlatformTransactionManager transactionManager,
                                                    DeleteQuestionUseCase delegate) {
        super(transactionManager);
        this.delegate = Objects.requireNonNull(delegate, "delegate must not be null");
    }

    @Override
    public Result<Void, List<DomainError>> execute(String id) {
        return executeInTransaction(() -> delegate.execute(id));
    }

    @Override
    public Result<DeleteQuestionsResultView, List<DomainError>> execute(List<String> ids) {
        return delegate.execute(ids);
    }
}

