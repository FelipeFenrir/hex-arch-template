package com.acme.orderquestionnaire.config.pipeline.transactional;

import com.acme.orderquestionnaire.application.questionnaire.dto.command.UpdateQuestionnaireCommand;
import com.acme.orderquestionnaire.application.questionnaire.dto.view.QuestionnaireUpdatedView;
import com.acme.orderquestionnaire.application.questionnaire.port.in.usecase.UpdateQuestionnaireUseCase;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;
import java.util.Objects;

public class TransactionalUpdateQuestionnaireUseCaseFacade extends TransactionalWriteUseCaseBoundary
        implements UpdateQuestionnaireUseCase {

    private final UpdateQuestionnaireUseCase delegate;

    public TransactionalUpdateQuestionnaireUseCaseFacade(PlatformTransactionManager transactionManager,
                                                         UpdateQuestionnaireUseCase delegate) {
        super(transactionManager);
        this.delegate = Objects.requireNonNull(delegate, "delegate must not be null");
    }

    @Override
    public Result<QuestionnaireUpdatedView, List<DomainError>> execute(UpdateQuestionnaireCommand command) {
        return executeInTransaction(() -> delegate.execute(command));
    }
}

