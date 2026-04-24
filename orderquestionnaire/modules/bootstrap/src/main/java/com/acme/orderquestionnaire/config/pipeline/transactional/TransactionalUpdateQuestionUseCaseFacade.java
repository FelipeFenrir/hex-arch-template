package com.acme.orderquestionnaire.config.pipeline.transactional;

import com.acme.orderquestionnaire.application.question.dto.command.UpdateQuestionCommand;
import com.acme.orderquestionnaire.application.question.dto.view.QuestionUpdatedView;
import com.acme.orderquestionnaire.application.question.port.in.usecase.UpdateQuestionUseCase;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;
import java.util.Objects;

public class TransactionalUpdateQuestionUseCaseFacade extends TransactionalWriteUseCaseBoundary
        implements UpdateQuestionUseCase {

    private final UpdateQuestionUseCase delegate;

    public TransactionalUpdateQuestionUseCaseFacade(PlatformTransactionManager transactionManager,
                                                    UpdateQuestionUseCase delegate) {
        super(transactionManager);
        this.delegate = Objects.requireNonNull(delegate, "delegate must not be null");
    }

    @Override
    public Result<QuestionUpdatedView, List<DomainError>> execute(String id, UpdateQuestionCommand command) {
        return executeInTransaction(() -> delegate.execute(id, command));
    }
}

