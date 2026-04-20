package com.acme.orderquestionnaire.application.questionnaire.service;

import com.acme.orderquestionnaire.application.questionnaire.dto.command.DeleteQuestionnaireCommand;
import com.acme.orderquestionnaire.application.questionnaire.dto.view.DeleteQuestionnaireFailureView;
import com.acme.orderquestionnaire.application.questionnaire.dto.view.DeleteQuestionnairesResultView;
import com.acme.orderquestionnaire.application.questionnaire.error.QuestionnaireErrors;
import com.acme.orderquestionnaire.application.questionnaire.port.in.usecase.DeleteQuestionnaireUseCase;
import com.acme.orderquestionnaire.application.questionnaire.port.out.repository.QuestionnaireCommandOutPort;
import com.acme.orderquestionnaire.domain.questionnaire.Questionnaire;
import com.acme.orderquestionnaire.domain.questionnaire.vo.QuestionnaireId;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Guard;
import com.acme.shared.pattern.result.Result;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class DeleteQuestionnaireService implements DeleteQuestionnaireUseCase {

    private final QuestionnaireCommandOutPort questionnaireRepository;

    public DeleteQuestionnaireService(QuestionnaireCommandOutPort questionnaireRepository) {
        this.questionnaireRepository = Objects.requireNonNull(questionnaireRepository,
                "questionnaireRepository must not be null");
    }

    @Override
    public Result<Void, List<DomainError>> execute(DeleteQuestionnaireCommand command) {
        Result<Void, List<DomainError>> validations = validateCommand(command);
        if (validations.isFailure()) {
            return Result.failure(validations.errorOrElseThrow(() ->
                    new IllegalStateException("Expected failure validations result")));
        }

        QuestionnaireId questionnaireId = QuestionnaireId.of(
                command.id(),
                command.channelDistributionId(),
                command.journeyDistributionId()
        );

        Optional<Questionnaire> existing = questionnaireRepository.findQuestionnaireById(questionnaireId);
        if (existing.isEmpty()) {
            return QuestionnaireErrors.QUESTIONNAIRE_NOT_FOUND.asFailure(command.id());
        }

        Questionnaire questionnaire = existing.get();
        if (!questionnaire.canBeDeleted()) {
            return QuestionnaireErrors.QUESTIONNAIRE_DELETE_NOT_ALLOWED.asFailure(command.id(), questionnaire.status().name());
        }

        Result<Void, List<DomainError>> deleteResult = questionnaireRepository.deleteById(questionnaireId);
        if (deleteResult.isFailure()) {
            List<DomainError> errors = deleteResult.errorOrElseThrow(() ->
                    new IllegalStateException("Expected delete failure result"));
            if (errors.isEmpty()) {
                return QuestionnaireErrors.QUESTIONNAIRE_DELETE_FAILED.asFailure(command.id(), "unknown reason");
            }
            return Result.failure(errors);
        }

        return Result.success(null);
    }

    @Override
    public Result<DeleteQuestionnairesResultView, List<DomainError>> execute(List<DeleteQuestionnaireCommand> commands) {
        if (commands == null || commands.isEmpty()) {
            return QuestionnaireErrors.INVALID_IDS.asFailure();
        }

        List<DeleteQuestionnaireFailureView> failures = new ArrayList<>();

        for (DeleteQuestionnaireCommand command : commands) {
            Optional<DeleteQuestionnaireFailureView> failure = deleteOneAndCollectFailure(command);
            failure.ifPresent(failures::add);
        }

        return Result.success(new DeleteQuestionnairesResultView(failures));
    }

    private Optional<DeleteQuestionnaireFailureView> deleteOneAndCollectFailure(DeleteQuestionnaireCommand command) {
        if (command == null) {
            DomainError error = QuestionnaireErrors.INVALID_COMMAND.toDomainError();
            return Optional.of(new DeleteQuestionnaireFailureView(null, null, null, error.code(), error.message()));
        }

        Result<Void, List<DomainError>> validations = validateCommand(command);
        if (validations.isFailure()) {
            DomainError firstError = validations.errorOrElseThrow(() ->
                    new IllegalStateException("Expected failure validations result")).getFirst();
            return Optional.of(new DeleteQuestionnaireFailureView(
                    command.id(),
                    command.channelDistributionId(),
                    command.journeyDistributionId(),
                    firstError.code(),
                    firstError.message()
            ));
        }

        QuestionnaireId questionnaireId = QuestionnaireId.of(
                command.id(),
                command.channelDistributionId(),
                command.journeyDistributionId()
        );

        Optional<Questionnaire> existing = questionnaireRepository.findQuestionnaireById(questionnaireId);
        if (existing.isEmpty()) {
            DomainError error = QuestionnaireErrors.QUESTIONNAIRE_NOT_FOUND.toDomainError(command.id());
            return Optional.of(new DeleteQuestionnaireFailureView(
                    command.id(),
                    command.channelDistributionId(),
                    command.journeyDistributionId(),
                    error.code(),
                    error.message()
            ));
        }

        Questionnaire questionnaire = existing.get();
        if (!questionnaire.canBeDeleted()) {
            DomainError error = QuestionnaireErrors.QUESTIONNAIRE_DELETE_NOT_ALLOWED
                    .toDomainError(command.id(), questionnaire.status().name());
            return Optional.of(new DeleteQuestionnaireFailureView(
                    command.id(),
                    command.channelDistributionId(),
                    command.journeyDistributionId(),
                    error.code(),
                    error.message()
            ));
        }

        Result<Void, List<DomainError>> deleteResult = questionnaireRepository.deleteById(questionnaireId);
        if (deleteResult.isFailure()) {
            List<DomainError> errors = deleteResult.errorOrElseThrow(() ->
                    new IllegalStateException("Expected delete failure result"));
            DomainError firstError = errors.isEmpty()
                    ? QuestionnaireErrors.QUESTIONNAIRE_DELETE_FAILED.toDomainError(command.id(), "unknown reason")
                    : errors.getFirst();
            return Optional.of(new DeleteQuestionnaireFailureView(
                    command.id(),
                    command.channelDistributionId(),
                    command.journeyDistributionId(),
                    firstError.code(),
                    firstError.message()
            ));
        }

        return Optional.empty();
    }

    private Result<Void, List<DomainError>> validateCommand(DeleteQuestionnaireCommand command) {
        if (command == null) {
            return QuestionnaireErrors.INVALID_COMMAND.asFailure();
        }

        return Guard.collect(List.of(
                Guard.requireNonBlank(command.id(), QuestionnaireErrors.INVALID_ID),
                Guard.requireNonBlank(command.channelDistributionId(), QuestionnaireErrors.INVALID_CHANNEL_DISTRIBUTION_ID),
                Guard.requireNonBlank(command.journeyDistributionId(), QuestionnaireErrors.INVALID_JOURNEY_DISTRIBUTION_ID)
        ));
    }
}

