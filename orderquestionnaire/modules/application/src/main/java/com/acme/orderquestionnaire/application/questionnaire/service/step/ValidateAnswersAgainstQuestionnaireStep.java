package com.acme.orderquestionnaire.application.questionnaire.service.step;

import com.acme.orderquestionnaire.application.questionnaire.dto.view.AnswerConfigurationView;
import com.acme.orderquestionnaire.application.questionnaire.dto.view.QuestionAnswerValidationView;
import com.acme.orderquestionnaire.application.questionnaire.dto.view.QuestionAnswerViolationView;
import com.acme.orderquestionnaire.application.questionnaire.dto.view.QuestionConditionView;
import com.acme.orderquestionnaire.application.questionnaire.dto.view.ValidateQuestionnaireAnswersView;
import com.acme.orderquestionnaire.application.questionnaire.service.context.ValidateQuestionnaireAnswersPipelineContext;
import com.acme.orderquestionnaire.application.questionnaire.service.support.ConditionQuestionIdExtractor;
import com.acme.orderquestionnaire.application.questionnaire.service.support.ViolationViewExtractor;
import com.acme.orderquestionnaire.domain.questionnaire.ConfiguredQuestion;
import com.acme.orderquestionnaire.domain.questionnaire.QuestionValidationFailure;
import com.acme.orderquestionnaire.domain.questionnaire.Questionnaire;
import com.acme.shared.pattern.pipeline.Step;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Executes domain answer validation and maps failures to view-level violations.
 */
public class ValidateAnswersAgainstQuestionnaireStep implements Step<ValidateQuestionnaireAnswersPipelineContext> {

    @Override
    public String id() {
        return "VALIDATE_QUESTIONNAIRE_ANSWERS";
    }

    @Override
    public Result<Void, List<DomainError>> execute(ValidateQuestionnaireAnswersPipelineContext context) {
        Questionnaire questionnaire = context.questionnaire();
        Map<String, Object> answers = context.command().answers();

        Result<Void, List<QuestionValidationFailure>> result = questionnaire.answerValidation(answers);
        if (result.isSuccess()) {
            context.validationView(new ValidateQuestionnaireAnswersView(
                    questionnaire.id(),
                    questionnaire.channelDistributionId(),
                    questionnaire.journeyDistributionId(),
                    true,
                    Map.of()
            ));
            return Result.success(null);
        }

        List<QuestionValidationFailure> failures = result.errorOrElseThrow(() ->
                new IllegalStateException("Expected failure answer validation result"));

        Map<String, ConfiguredQuestion> configuredByQuestionId = questionnaire.configuredQuestions().stream()
                .collect(Collectors.toMap(
                        configured -> configured.question().id(),
                        configured -> configured,
                        (left, right) -> right,
                        LinkedHashMap::new
                ));

        LinkedHashMap<String, QuestionAnswerValidationView> mappedViolations = new LinkedHashMap<>();
        failures.stream()
                .sorted(Comparator.comparingInt(QuestionValidationFailure::order))
                .forEach(failure -> {
                    ConfiguredQuestion configuredQuestion = configuredByQuestionId.get(failure.questionId());
                    mappedViolations.put(
                            failure.questionId(),
                            mapQuestionViolation(failure, configuredQuestion, answers, configuredByQuestionId)
                    );
                });

        context.validationView(new ValidateQuestionnaireAnswersView(
                questionnaire.id(),
                questionnaire.channelDistributionId(),
                questionnaire.journeyDistributionId(),
                false,
                mappedViolations
        ));

        return Result.success(null);
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

