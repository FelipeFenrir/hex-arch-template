package com.acme.orderquestionnaire.application.questionnaire.port.in.usecase;

import com.acme.orderquestionnaire.application.questionnaire.dto.command.ValidateQuestionnaireAnswersCommand;
import com.acme.orderquestionnaire.application.questionnaire.dto.view.ValidateQuestionnaireAnswersView;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;
import com.acme.shared.stereotypes.core.UseCase;

import java.util.List;

@UseCase
public interface ValidateQuestionnaireAnswersUseCase {
    Result<ValidateQuestionnaireAnswersView, List<DomainError>> execute(ValidateQuestionnaireAnswersCommand command);
}

