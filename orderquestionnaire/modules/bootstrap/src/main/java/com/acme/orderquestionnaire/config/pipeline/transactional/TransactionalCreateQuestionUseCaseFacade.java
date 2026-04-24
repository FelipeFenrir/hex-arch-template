package com.acme.orderquestionnaire.config.pipeline.transactional;

import com.acme.orderquestionnaire.application.question.dto.command.CreateQuestionCommand;
import com.acme.orderquestionnaire.application.question.dto.view.QuestionCreatedView;
import com.acme.orderquestionnaire.application.question.port.in.usecase.CreateQuestionUseCase;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;
import java.util.Objects;

public class TransactionalCreateQuestionUseCaseFacade extends TransactionalWriteUseCaseBoundary
        implements CreateQuestionUseCase {

    private final CreateQuestionUseCase delegate;

    public TransactionalCreateQuestionUseCaseFacade(PlatformTransactionManager transactionManager,
                                                    CreateQuestionUseCase delegate) {
        super(transactionManager);
        this.delegate = Objects.requireNonNull(delegate, "delegate must not be null");
    }

    @Override
    public Result<QuestionCreatedView, List<DomainError>> execute(CreateQuestionCommand command) {
        return executeInTransaction(() -> delegate.execute(command));
    }
}

