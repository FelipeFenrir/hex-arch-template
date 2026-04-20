package com.acme.orderquestionnaire.application.question.port.in.usecase;

import com.acme.orderquestionnaire.application.question.dto.command.CreateQuestionCommand;
import com.acme.orderquestionnaire.application.question.dto.view.QuestionCreatedView;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;
import com.acme.shared.stereotypes.core.UseCase;

import java.util.List;

@UseCase
public interface CreateQuestionUseCase {
    Result<QuestionCreatedView, List<DomainError>> execute(CreateQuestionCommand command);
}
