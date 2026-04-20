package com.acme.orderquestionnaire.application.question.service;

import com.acme.orderquestionnaire.application.question.error.QuestionErrors;
import com.acme.orderquestionnaire.application.question.dto.command.UpdateQuestionCommand;
import com.acme.orderquestionnaire.application.audit.dto.command.AuditUserParam;
import com.acme.orderquestionnaire.application.question.port.in.usecase.UpdateQuestionUseCase;
import com.acme.orderquestionnaire.application.question.port.out.repository.QuestionCommandOutPort;
import com.acme.orderquestionnaire.application.question.dto.view.QuestionUpdatedView;
import com.acme.orderquestionnaire.domain.audit.OrderQuestionnaireAuditFactory;
import com.acme.orderquestionnaire.domain.question.Question;
import com.acme.orderquestionnaire.domain.question.QuestionFactory;
import com.acme.orderquestionnaire.domain.question.QuestionStatusMachine;
import com.acme.orderquestionnaire.domain.question.QuestionStatusTransitionContext;
import com.acme.shared.engine.state.TransitionResult;
import com.acme.shared.enumerator.ParameterizationStatus;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Guard;
import com.acme.shared.pattern.result.Result;
import com.acme.shared.vo.AuditInfo;
import com.acme.shared.vo.AuditUser;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class UpdateQuestionService implements UpdateQuestionUseCase {

    private final QuestionCommandOutPort questionRepository;

    public UpdateQuestionService(QuestionCommandOutPort questionRepository) {
        this.questionRepository = Objects.requireNonNull(questionRepository, "questionRepository must not be null");
    }

    @Override
    public Result<QuestionUpdatedView, List<DomainError>> execute(String id, UpdateQuestionCommand command) {
        Result<Void, List<DomainError>> validations = validateCommand(id, command);
        if (validations.isFailure()) {
            return Result.failure(validations.errorOrElseThrow(() ->
                    new IllegalStateException("Expected failure validations result")));
        }

        return validations.flatMap(__ -> resolveUpdatedActor(command)
                    .flatMap(actor -> findExistingQuestion(id)
                        .flatMap(current -> resolveTransition(current, command, actor)
                            .flatMap(transition ->
                                    buildUpdatedQuestion(id, command, transition))
                        )
                    )
                )
                .flatMap(questionRepository::update)
                .map(QuestionUpdatedView::from);
    }

    private Result<Void, List<DomainError>> validateCommand(String id, UpdateQuestionCommand command) {
        if (command == null) {
            return QuestionErrors.INVALID_COMMAND.asFailure();
        }

        return Guard.collect(List.of(
                QuestionFactory.validateUpdatePayload(id, command.label(), command.salesItemReferenceCode())
        ));
    }

    private Result<AuditUser, List<DomainError>> resolveUpdatedActor(UpdateQuestionCommand command) {
        AuditUserParam updatedBy = command.updatedBy();
        return OrderQuestionnaireAuditFactory.updateUserForQuestion(
                updatedBy == null ? null : updatedBy.id(),
                updatedBy == null ? null : updatedBy.referenceCode(),
                updatedBy == null ? null : updatedBy.name(),
                updatedBy == null ? null : updatedBy.email(),
                command.updatedAt());
    }

    private Result<Question, List<DomainError>> findExistingQuestion(String id) {
        Optional<Question> existingQuestion = questionRepository.findQuestionById(id);
        return existingQuestion
                .map(Result::<Question, List<DomainError>>success)
                .orElseGet(QuestionErrors.QUESTION_NOT_FOUND::asFailure);
    }

    private Result<TransitionResult<ParameterizationStatus, QuestionStatusTransitionContext>, List<DomainError>>
    resolveTransition(Question current, UpdateQuestionCommand command, AuditUser actor) {

        QuestionStatusTransitionContext context = new QuestionStatusTransitionContext(
                current,
                actor,
                command.updatedAt(),
                current.auditInfo().withUpdate(actor, command.updatedAt()),
                false
        );

        final ParameterizationStatus desiredStatus = command.status() == null
                ? current.status()
                : command.status();
        return QuestionStatusMachine.transition(current.status(), desiredStatus, context);
    }

    private Result<Question, List<DomainError>> buildUpdatedQuestion(String id,
                                                                     UpdateQuestionCommand command,
                                                                     TransitionResult<ParameterizationStatus,
                                                                     QuestionStatusTransitionContext> transition) {
        AuditInfo updatedAuditInfo = transition.context().auditInfo();

        return QuestionFactory.rehydrate(id, command.label(), transition.targetState(), updatedAuditInfo)
                .flatMap(builder -> builder
                        .withSalesItemReferenceCode(command.salesItemReferenceCode())
                        .build());
    }
}

