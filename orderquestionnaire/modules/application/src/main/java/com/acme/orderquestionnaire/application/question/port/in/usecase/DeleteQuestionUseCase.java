package com.acme.orderquestionnaire.application.question.port.in.usecase;

import com.acme.orderquestionnaire.application.question.dto.view.DeleteQuestionsResultView;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;
import com.acme.shared.stereotypes.core.UseCase;

import java.util.List;

@UseCase
public interface DeleteQuestionUseCase {
    Result<Void, List<DomainError>> execute(String id);
    Result<DeleteQuestionsResultView, List<DomainError>> execute(List<String> ids);
}
