package com.acme.orderquestionnaire.application.question.service;

import com.acme.orderquestionnaire.application.question.error.QuestionErrors;
import com.acme.orderquestionnaire.application.question.dto.command.CreateQuestionCommand;
import com.acme.orderquestionnaire.application.audit.dto.command.AuditUserParam;
import com.acme.orderquestionnaire.application.question.port.in.usecase.CreateQuestionUseCase;
import com.acme.orderquestionnaire.application.question.port.out.repository.QuestionCommandOutPort;
import com.acme.orderquestionnaire.application.question.dto.view.QuestionCreatedView;
import com.acme.orderquestionnaire.domain.audit.OrderQuestionnaireAuditFactory;
import com.acme.orderquestionnaire.domain.question.QuestionFactory;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Guard;
import com.acme.shared.pattern.result.Result;
import com.acme.shared.vo.AuditInfo;

import java.util.List;
import java.util.Objects;

public class CreateQuestionService implements CreateQuestionUseCase {

    private final QuestionCommandOutPort questionRepository;

    public CreateQuestionService(QuestionCommandOutPort questionRepository) {

        this.questionRepository = Objects.requireNonNull(questionRepository,
                "questionRepository must not be null");
    }

     @Override
     public Result<QuestionCreatedView, List<DomainError>> execute(CreateQuestionCommand command) {
         Result<Void, List<DomainError>> validations = validateCommand(command);
         if (validations.isFailure()) {
             return Result.failure(validations.errorOrElseThrow(() ->
                     new IllegalStateException("Expected failure validations result")));
         }

         return validations
                 .flatMap(__ -> checkNoDuplicate(command.id()))
                 .flatMap(__ -> buildCreatedAudit(command))
                 .flatMap(auditInfo -> QuestionFactory.createNew(
                         command.id(),
                         command.label(),
                         command.salesItemReferenceCode(),
                         auditInfo))
                 .flatMap(builder -> builder.build())
                 .flatMap(questionRepository::create)
                 .map(QuestionCreatedView::from);
     }

    private Result<Void, List<DomainError>> validateCommand(CreateQuestionCommand command) {
        if (command == null) {
            return QuestionErrors.INVALID_COMMAND.asFailure();
        }

        return Guard.collect(List.of(
                QuestionFactory.validateCreatePayload(command.id(), command.label(), command.salesItemReferenceCode())
        ));
    }

    private Result<AuditInfo, List<DomainError>> buildCreatedAudit(CreateQuestionCommand command) {
        AuditUserParam createdBy = command.createdBy();
        return OrderQuestionnaireAuditFactory.createNewForQuestion(
                createdBy == null ? null : createdBy.id(),
                createdBy == null ? null : createdBy.referenceCode(),
                createdBy == null ? null : createdBy.name(),
                createdBy == null ? null : createdBy.email(),
                command.createdAt());
    }


    private Result<Void, List<DomainError>> checkNoDuplicate(String id) {
        return questionRepository.existsById(id)
                ? QuestionErrors.QUESTION_ALREADY_EXISTS.asFailure(id)
                : Result.success(null);
    }
}
