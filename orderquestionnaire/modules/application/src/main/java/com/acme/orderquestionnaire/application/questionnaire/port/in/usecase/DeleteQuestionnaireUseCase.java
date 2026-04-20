package com.acme.orderquestionnaire.application.questionnaire.port.in.usecase;

import com.acme.orderquestionnaire.application.questionnaire.dto.command.DeleteQuestionnaireCommand;
import com.acme.orderquestionnaire.application.questionnaire.dto.view.DeleteQuestionnairesResultView;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;
import com.acme.shared.stereotypes.core.UseCase;

import java.util.List;

@UseCase
public interface DeleteQuestionnaireUseCase {
    Result<Void, List<DomainError>> execute(DeleteQuestionnaireCommand command);
    Result<DeleteQuestionnairesResultView, List<DomainError>> execute(List<DeleteQuestionnaireCommand> commands);
}
