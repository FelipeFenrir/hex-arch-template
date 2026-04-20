package com.acme.orderquestionnaire.application.questionnaire.service;

import com.acme.orderquestionnaire.application.questionnaire.dto.command.ValidateQuestionnaireAnswersCommand;
import com.acme.orderquestionnaire.application.questionnaire.dto.view.AnswerConfigurationView;
import com.acme.orderquestionnaire.application.questionnaire.dto.view.QuestionAnswerValidationView;
import com.acme.orderquestionnaire.application.questionnaire.dto.view.QuestionAnswerViolationView;
import com.acme.orderquestionnaire.application.questionnaire.dto.view.QuestionConditionView;
import com.acme.orderquestionnaire.application.questionnaire.dto.view.ValidateQuestionnaireAnswersView;
import com.acme.orderquestionnaire.application.questionnaire.error.QuestionnaireErrors;
import com.acme.orderquestionnaire.application.questionnaire.port.in.usecase.ValidateQuestionnaireAnswersUseCase;
import com.acme.orderquestionnaire.application.questionnaire.port.out.repository.QuestionnaireCommandOutPort;
import com.acme.orderquestionnaire.application.questionnaire.service.support.ConditionQuestionIdExtractor;
import com.acme.orderquestionnaire.application.questionnaire.service.support.ViolationViewExtractor;
import com.acme.orderquestionnaire.domain.questionnaire.ConfiguredQuestion;
import com.acme.orderquestionnaire.domain.questionnaire.QuestionValidationFailure;
import com.acme.orderquestionnaire.domain.questionnaire.Questionnaire;
import com.acme.orderquestionnaire.domain.questionnaire.vo.QuestionnaireId;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Guard;
import com.acme.shared.pattern.result.Result;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ValidateQuestionnaireAnswersService implements ValidateQuestionnaireAnswersUseCase {

    private final QuestionnaireCommandOutPort questionnaireRepository;

    public ValidateQuestionnaireAnswersService(QuestionnaireCommandOutPort questionnaireRepository) {
        this.questionnaireRepository = Objects.requireNonNull(questionnaireRepository,
                "questionnaireRepository must not be null");
    }

    @Override
    public Result<ValidateQuestionnaireAnswersView, List<DomainError>> execute(ValidateQuestionnaireAnswersCommand command) {
        Result<Void, List<DomainError>> validations = validateCommand(command);
        if (validations.isFailure()) {
            return Result.failure(validations.errorOrElseThrow(() ->
                    new IllegalStateException("Expected failure validations result")));
        }

        return validations
                .flatMap(__ -> fetchQuestionnaire(command))
                .flatMap(questionnaire -> validateAnswers(questionnaire, command));
    }

    private Result<Void, List<DomainError>> validateCommand(ValidateQuestionnaireAnswersCommand command) {
        if (command == null) {
            return QuestionnaireErrors.INVALID_COMMAND.asFailure();
        }

        return Guard.collect(List.of(
                Guard.requireNonBlank(command.questionnaireId(), QuestionnaireErrors.INVALID_ID),
                Guard.requireNonBlank(
                        command.channelDistributionId(),
                        QuestionnaireErrors.INVALID_CHANNEL_DISTRIBUTION_ID
                ),
                Guard.requireNonBlank(
                        command.journeyDistributionId(),
                        QuestionnaireErrors.INVALID_JOURNEY_DISTRIBUTION_ID
                ),
                Guard.requireNonNull(command.answers(), QuestionnaireErrors.INVALID_ANSWERS)
        ));
    }

    private Result<Questionnaire, List<DomainError>> fetchQuestionnaire(ValidateQuestionnaireAnswersCommand command) {
        QuestionnaireId id = QuestionnaireId.of(
                command.questionnaireId(),
                command.channelDistributionId(),
                command.journeyDistributionId());

        Optional<Questionnaire> questionnaire = questionnaireRepository.findQuestionnaireById(id);
        return questionnaire
                .<Result<Questionnaire, List<DomainError>>>map(Result::success)
                .orElseGet(() -> QuestionnaireErrors.QUESTIONNAIRE_NOT_FOUND.asFailure(command.questionnaireId()));
    }

    private Result<ValidateQuestionnaireAnswersView, List<DomainError>> validateAnswers(Questionnaire questionnaire,
                                                                                         ValidateQuestionnaireAnswersCommand command) {
        Map<String, Object> answers = command.answers();
        Result<Void, List<QuestionValidationFailure>> result = questionnaire.answerValidation(answers);

        if (result.isSuccess()) {
            return Result.success(new ValidateQuestionnaireAnswersView(
                    questionnaire.id(),
                    questionnaire.channelDistributionId(),
                    questionnaire.journeyDistributionId(),
                    true,
                    Map.of()));
        }

        List<QuestionValidationFailure> failures = result.errorOrElseThrow(() ->
                new IllegalStateException("Expected failure answer validation result"));

        Map<String, ConfiguredQuestion> configuredByQuestionId = questionnaire.configuredQuestions().stream()
                .collect(Collectors.toMap(
                        configured -> configured.question().id(),
                        configured -> configured,
                        (left, right) -> right,
                        LinkedHashMap::new));

        LinkedHashMap<String, QuestionAnswerValidationView> mappedViolations = new LinkedHashMap<>();
        failures.stream()
                .sorted(Comparator.comparingInt(QuestionValidationFailure::order))
                .forEach(failure -> {
                    ConfiguredQuestion configuredQuestion = configuredByQuestionId.get(failure.questionId());
                    mappedViolations.put(failure.questionId(),
                            mapQuestionViolation(failure, configuredQuestion, answers, configuredByQuestionId));
                });

        return Result.success(new ValidateQuestionnaireAnswersView(
                questionnaire.id(),
                questionnaire.channelDistributionId(),
                questionnaire.journeyDistributionId(),
                false,
                mappedViolations));
    }

    private QuestionAnswerValidationView mapQuestionViolation(QuestionValidationFailure failure,
                                                               ConfiguredQuestion configuredQuestion,
                                                               Map<String, Object> answers,
                                                               Map<String, ConfiguredQuestion> configuredByQuestionId) {
        Object providedAnswer = answers.get(failure.questionId());

        AnswerConfigurationView answerRule = configuredQuestion == null
                ? null
                : AnswerConfigurationView.from(configuredQuestion.answerConfiguration());

        QuestionConditionView conditionRule = configuredQuestion == null
                ? null
                : QuestionConditionView.from(configuredQuestion.rootCondition());

        Set<String> dependsOnQuestionIds = configuredQuestion == null
                ? Set.of()
                : ConditionQuestionIdExtractor.extract(configuredQuestion.rootCondition());

        boolean visibleByCondition = configuredQuestion == null || configuredQuestion.isVisible(answers);

        List<QuestionAnswerViolationView> violations = failure.errors().stream()
                .map(error -> ViolationViewExtractor.build(error, configuredQuestion, configuredByQuestionId))
                .toList();

        return new QuestionAnswerValidationView(
                failure.questionId(),
                failure.questionLabel(),
                failure.order(),
                providedAnswer,
                visibleByCondition,
                answerRule,
                conditionRule,
                dependsOnQuestionIds,
                violations
        );
    }
}

