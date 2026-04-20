package com.acme.orderquestionnaire.application.question.port.in.usecase;

import com.acme.orderquestionnaire.application.question.dto.command.UpdateQuestionCommand;
import com.acme.orderquestionnaire.application.question.dto.view.QuestionUpdatedView;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;
import com.acme.shared.stereotypes.core.UseCase;

import java.util.List;

@UseCase
public interface UpdateQuestionUseCase {
    Result<QuestionUpdatedView, List<DomainError>> execute(String id, UpdateQuestionCommand command);
}
