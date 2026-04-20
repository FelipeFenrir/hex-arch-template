package com.acme.orderquestionnaire.application.questionnaire.service;

import com.acme.orderquestionnaire.application.question.error.QuestionErrors;
import com.acme.orderquestionnaire.application.question.port.out.repository.QuestionCommandOutPort;
import com.acme.orderquestionnaire.application.questionnaire.dto.command.UpdateConfiguredQuestionParam;
import com.acme.orderquestionnaire.application.questionnaire.dto.command.UpdateQuestionnaireCommand;
import com.acme.orderquestionnaire.application.questionnaire.dto.view.QuestionnaireUpdatedView;
import com.acme.orderquestionnaire.application.questionnaire.error.QuestionnaireErrors;
import com.acme.orderquestionnaire.application.audit.dto.command.AuditUserParam;
import com.acme.orderquestionnaire.application.questionnaire.port.in.usecase.UpdateQuestionnaireUseCase;
import com.acme.orderquestionnaire.application.questionnaire.port.out.repository.QuestionnaireCommandOutPort;
import com.acme.orderquestionnaire.application.questionnaire.service.support.ConditionQuestionIdExtractor;
import com.acme.orderquestionnaire.application.questionnaire.service.support.ConfiguredQuestionAssembler;
import com.acme.orderquestionnaire.domain.audit.OrderQuestionnaireAuditFactory;
import com.acme.orderquestionnaire.domain.question.Question;
import com.acme.orderquestionnaire.domain.question.QuestionFactory;
import com.acme.orderquestionnaire.domain.questionnaire.ConfiguredQuestion;
import com.acme.orderquestionnaire.domain.questionnaire.Questionnaire;
import com.acme.orderquestionnaire.domain.questionnaire.QuestionnaireStatusMachine;
import com.acme.orderquestionnaire.domain.questionnaire.QuestionnaireStatusTransitionContext;
import com.acme.orderquestionnaire.domain.questionnaire.vo.QuestionnaireId;
import com.acme.shared.engine.state.TransitionResult;
import com.acme.shared.enumerator.ParameterizationStatus;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Guard;
import com.acme.shared.pattern.result.Result;
import com.acme.shared.vo.AuditInfo;
import com.acme.shared.vo.AuditUser;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class UpdateQuestionnaireService implements UpdateQuestionnaireUseCase {

    private final QuestionnaireCommandOutPort questionnaireRepository;
    private final QuestionCommandOutPort questionRepository;

    public UpdateQuestionnaireService(QuestionnaireCommandOutPort questionnaireRepository,
                                      QuestionCommandOutPort questionRepository) {
        this.questionnaireRepository = Objects.requireNonNull(questionnaireRepository,
                "questionnaireRepository must not be null");
        this.questionRepository = Objects.requireNonNull(questionRepository,
                "questionRepository must not be null");
    }

    @Override
    public Result<QuestionnaireUpdatedView, List<DomainError>> execute(UpdateQuestionnaireCommand command) {
        Result<Void, List<DomainError>> validations = validateCommand(command);
        if (validations.isFailure()) {
            return Result.failure(validations.errorOrElseThrow(() ->
                    new IllegalStateException("Expected failure validations result")));
        }

        return validations
                .flatMap(__ -> resolveUpdatedActor(command)
                        .flatMap(actor -> fetchExisting(command)
                                .flatMap(existing -> validateActiveUpdateRestrictions(existing, command)
                                        .flatMap(ignored -> resolveConfiguredQuestions(existing, command)
                                                .flatMap(finalQuestions -> applyUpdate(existing, command, finalQuestions, actor))))))
                .flatMap(questionnaireRepository::update)
                .map(QuestionnaireUpdatedView::from);
    }

    private Result<Void, List<DomainError>> validateCommand(UpdateQuestionnaireCommand command) {
        if (command == null) {
            return QuestionnaireErrors.INVALID_COMMAND.asFailure();
        }

        List<Result<Void, List<DomainError>>> validations = new ArrayList<>();
        validations.add(Guard.requireNonBlank(command.id(), QuestionnaireErrors.INVALID_ID));
        validations.add(Guard.requireNonBlank(
                command.channelDistributionId(),
                QuestionnaireErrors.INVALID_CHANNEL_DISTRIBUTION_ID
        ));
        validations.add(Guard.requireNonBlank(
                command.journeyDistributionId(),
                QuestionnaireErrors.INVALID_JOURNEY_DISTRIBUTION_ID
        ));
        if (command.description() != null) {
            validations.add(Guard.requireNonBlank(command.description(), QuestionnaireErrors.INVALID_DESCRIPTION));
        }

        for (UpdateConfiguredQuestionParam upsert : safeList(command.questionsToUpsert())) {
            validations.add(Guard.requireNonNull(upsert, QuestionnaireErrors.INVALID_COMMAND));
            if (upsert != null) {
                validations.add(QuestionFactory.validateQuestionId(upsert.questionId()));
                validations.add(Guard.requireNonNull(upsert.param(), QuestionnaireErrors.INVALID_COMMAND));
            }
        }

        return Guard.collect(validations);
    }

    private Result<AuditUser, List<DomainError>> resolveUpdatedActor(UpdateQuestionnaireCommand command) {
        AuditUserParam updatedBy = command.updatedBy();
        return OrderQuestionnaireAuditFactory.updateUserForQuestionnaire(
                updatedBy == null ? null : updatedBy.id(),
                updatedBy == null ? null : updatedBy.referenceCode(),
                updatedBy == null ? null : updatedBy.name(),
                updatedBy == null ? null : updatedBy.email(),
                command.updatedAt());
    }

    private Result<Questionnaire, List<DomainError>> fetchExisting(UpdateQuestionnaireCommand command) {
        QuestionnaireId questionnaireId = QuestionnaireId.of(
                command.id(),
                command.channelDistributionId(),
                command.journeyDistributionId());

        Optional<Questionnaire> existing = questionnaireRepository.findQuestionnaireById(questionnaireId);
        return existing
                .<Result<Questionnaire, List<DomainError>>>map(Result::success)
                .orElseGet(() -> QuestionnaireErrors.QUESTIONNAIRE_NOT_FOUND.asFailure(command.id()));
    }

    private Result<Void, List<DomainError>> validateActiveUpdateRestrictions(Questionnaire existing,
                                                                              UpdateQuestionnaireCommand command) {
        if (existing.status() != ParameterizationStatus.ACTIVE) {
            return Result.success(null);
        }

        boolean hasStructuralChanges = hasStructuralChanges(command);
        boolean hasDescriptionChange = command.description() != null && !command.description().equals(existing.description());
        boolean wantsDeactivate = command.status() == ParameterizationStatus.INACTIVE;

        if (wantsDeactivate && !hasStructuralChanges && !hasDescriptionChange) {
            return Result.success(null);
        }

        return QuestionnaireErrors.QUESTIONNAIRE_UPDATE_NOT_ALLOWED.asFailure();
    }

    private boolean hasStructuralChanges(UpdateQuestionnaireCommand command) {
        return (command.questionsToUpsert() != null && !command.questionsToUpsert().isEmpty())
                || (command.questionIdsToRemove() != null && !command.questionIdsToRemove().isEmpty());
    }

    private Result<List<ConfiguredQuestion>, List<DomainError>> resolveConfiguredQuestions(Questionnaire existing,
                                                                                            UpdateQuestionnaireCommand command) {
        Map<String, ConfiguredQuestion> byQuestionId = existing.configuredQuestions().stream()
                .collect(Collectors.toMap(
                        configuredQuestion -> configuredQuestion.question().id(),
                        configuredQuestion -> configuredQuestion,
                        (left, right) -> right,
                        LinkedHashMap::new));

        for (String questionIdToRemove : safeList(command.questionIdsToRemove())) {
            if (questionIdToRemove != null) {
                byQuestionId.remove(questionIdToRemove);
            }
        }

        for (UpdateConfiguredQuestionParam upsert : safeList(command.questionsToUpsert())) {
            Result<ConfiguredQuestion, List<DomainError>> assembled = assembleConfiguredQuestion(upsert);
            if (assembled.isFailure()) {
                return Result.failure(assembled.errorOrElseThrow(() ->
                        new IllegalStateException("Expected failure result")));
            }
            ConfiguredQuestion configuredQuestion = assembled.getOrElseThrow(error ->
                    new IllegalStateException("Expected success result"));
            byQuestionId.put(configuredQuestion.question().id(), configuredQuestion);
        }

        List<ConfiguredQuestion> finalQuestions = new ArrayList<>(byQuestionId.values());
        return validateConditionRefs(finalQuestions).map(__ -> finalQuestions);
    }

    private Result<ConfiguredQuestion, List<DomainError>> assembleConfiguredQuestion(UpdateConfiguredQuestionParam upsert) {
        return Guard.requireNonNull(upsert, QuestionnaireErrors.INVALID_COMMAND)
                .flatMap(__ -> Guard.requireNonBlank(upsert.questionId(), QuestionnaireErrors.INVALID_ID))
                .flatMap(__ -> Guard.requireNonNull(upsert.param(), QuestionnaireErrors.INVALID_COMMAND))
                .flatMap(__ -> fetchQuestion(upsert.questionId()))
                .flatMap(question -> ConfiguredQuestionAssembler.build(question, upsert.param()));
    }

    private Result<Question, List<DomainError>> fetchQuestion(String questionId) {
        return questionRepository.findQuestionById(questionId)
                .<Result<Question, List<DomainError>>>map(Result::success)
                .orElseGet(QuestionErrors.QUESTION_NOT_FOUND::asFailure);
    }

    private Result<Void, List<DomainError>> validateConditionRefs(List<ConfiguredQuestion> configuredQuestions) {
        Set<String> existingIds = configuredQuestions.stream()
                .map(configuredQuestion -> configuredQuestion.question().id())
                .collect(Collectors.toSet());

        for (ConfiguredQuestion configuredQuestion : configuredQuestions) {
            if (configuredQuestion.rootCondition() == null) {
                continue;
            }

            for (String refId : ConditionQuestionIdExtractor.extract(configuredQuestion.rootCondition())) {
                if (!existingIds.contains(refId)) {
                    return QuestionnaireErrors.CONDITION_QUESTION_NOT_FOUND.asFailure(refId);
                }
            }
        }

        return Result.success(null);
    }

    private Result<Questionnaire, List<DomainError>> applyUpdate(Questionnaire existing,
                                                                  UpdateQuestionnaireCommand command,
                                                                  List<ConfiguredQuestion> finalQuestions,
                                                                  AuditUser actor) {
        String updatedDescription = resolveDescription(existing, command);
        if (updatedDescription == null || updatedDescription.isBlank()) {
            return QuestionnaireErrors.INVALID_DESCRIPTION.asFailure();
        }

        ParameterizationStatus desiredStatus = command.status() == null ? existing.status() : command.status();

        Questionnaire candidate = Questionnaire.rehydrate(
                existing.questionnaireId(),
                updatedDescription,
                existing.status(),
                finalQuestions,
                existing.auditInfo());

        AuditInfo updatedAudit = existing.auditInfo().withUpdate(actor, command.updatedAt());
        QuestionnaireStatusTransitionContext context = new QuestionnaireStatusTransitionContext(
                candidate,
                actor,
                command.updatedAt(),
                updatedAudit,
                false
        );

        Result<TransitionResult<ParameterizationStatus, QuestionnaireStatusTransitionContext>, List<DomainError>> transition =
                QuestionnaireStatusMachine.transition(existing.status(), desiredStatus, context);

        if (transition.isFailure()) {
            return Result.failure(transition.errorOrElseThrow(() ->
                    new IllegalStateException("Expected failure transition result")));
        }

        TransitionResult<ParameterizationStatus, QuestionnaireStatusTransitionContext> result =
                transition.getOrElseThrow(error ->
                        new IllegalStateException("Expected success transition result"));

        return Result.success(Questionnaire.rehydrate(
                existing.questionnaireId(),
                updatedDescription,
                result.targetState(),
                finalQuestions,
                result.context().auditInfo()));
    }

    private String resolveDescription(Questionnaire existing, UpdateQuestionnaireCommand command) {
        return command.description() == null ? existing.description() : command.description();
    }

    private <T> List<T> safeList(List<T> value) {
        return value == null ? List.of() : value;
    }
}
