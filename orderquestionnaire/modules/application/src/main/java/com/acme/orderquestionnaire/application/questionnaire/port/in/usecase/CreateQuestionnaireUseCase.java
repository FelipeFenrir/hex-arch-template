package com.acme.orderquestionnaire.application.questionnaire.port.in.usecase;

import com.acme.orderquestionnaire.application.questionnaire.dto.command.CreateQuestionnaireCommand;
import com.acme.orderquestionnaire.application.questionnaire.dto.view.QuestionnaireCreatedView;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;
import com.acme.shared.stereotypes.core.UseCase;

import java.util.List;

@UseCase
public interface CreateQuestionnaireUseCase {
    Result<QuestionnaireCreatedView, List<DomainError>> execute(CreateQuestionnaireCommand command);
}
