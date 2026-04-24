package com.acme.orderquestionnaire.config.pipeline.transactional;

import com.acme.orderquestionnaire.application.questionnaire.dto.command.CreateQuestionnaireCommand;
import com.acme.orderquestionnaire.application.questionnaire.dto.view.QuestionnaireCreatedView;
import com.acme.orderquestionnaire.application.questionnaire.port.in.usecase.CreateQuestionnaireUseCase;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;
import java.util.Objects;

public class TransactionalCreateQuestionnaireUseCaseFacade extends TransactionalWriteUseCaseBoundary
        implements CreateQuestionnaireUseCase {

    private final CreateQuestionnaireUseCase delegate;

    public TransactionalCreateQuestionnaireUseCaseFacade(PlatformTransactionManager transactionManager,
                                                         CreateQuestionnaireUseCase delegate) {
        super(transactionManager);
        this.delegate = Objects.requireNonNull(delegate, "delegate must not be null");
    }

    @Override
    public Result<QuestionnaireCreatedView, List<DomainError>> execute(CreateQuestionnaireCommand command) {
        return executeInTransaction(() -> delegate.execute(command));
    }
}

