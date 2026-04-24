package com.acme.orderquestionnaire.config.pipeline.transactional;

import com.acme.orderquestionnaire.application.questionnaire.dto.command.DeleteQuestionnaireCommand;
import com.acme.orderquestionnaire.application.questionnaire.dto.view.DeleteQuestionnairesResultView;
import com.acme.orderquestionnaire.application.questionnaire.port.in.usecase.DeleteQuestionnaireUseCase;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;
import java.util.Objects;

public class TransactionalDeleteQuestionnaireUseCaseFacade extends TransactionalWriteUseCaseBoundary
        implements DeleteQuestionnaireUseCase {

    private final DeleteQuestionnaireUseCase delegate;

    public TransactionalDeleteQuestionnaireUseCaseFacade(PlatformTransactionManager transactionManager,
                                                         DeleteQuestionnaireUseCase delegate) {
        super(transactionManager);
        this.delegate = Objects.requireNonNull(delegate, "delegate must not be null");
    }

    @Override
    public Result<Void, List<DomainError>> execute(DeleteQuestionnaireCommand command) {
        return executeInTransaction(() -> delegate.execute(command));
    }

    @Override
    public Result<DeleteQuestionnairesResultView, List<DomainError>> execute(List<DeleteQuestionnaireCommand> commands) {
        return delegate.execute(commands);
    }
}

