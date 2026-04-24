package com.acme.orderquestionnaire.application.questionnaire.service.step;

import com.acme.orderquestionnaire.application.question.error.QuestionErrors;
import com.acme.orderquestionnaire.application.question.port.out.repository.QuestionCommandOutPort;
import com.acme.orderquestionnaire.application.questionnaire.dto.command.UpdateConfiguredQuestionParam;
import com.acme.orderquestionnaire.application.questionnaire.error.QuestionnaireErrors;
import com.acme.orderquestionnaire.application.questionnaire.service.context.UpdateQuestionnairePipelineContext;
import com.acme.orderquestionnaire.application.questionnaire.service.support.ConditionQuestionIdExtractor;
import com.acme.orderquestionnaire.application.questionnaire.service.support.ConfiguredQuestionAssembler;
import com.acme.orderquestionnaire.domain.question.Question;
import com.acme.orderquestionnaire.domain.questionnaire.ConfiguredQuestion;
import com.acme.shared.pattern.pipeline.Step;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Guard;
import com.acme.shared.pattern.result.Result;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class ResolveConfiguredQuestionsStep implements Step<UpdateQuestionnairePipelineContext> {

    private final QuestionCommandOutPort questionCommandOutPort;

    public ResolveConfiguredQuestionsStep(QuestionCommandOutPort questionCommandOutPort) {
        this.questionCommandOutPort = Objects.requireNonNull(questionCommandOutPort,
                "questionCommandOutPort must not be null");
    }

    @Override
    public String id() {
        return "RESOLVE_CONFIGURED_QUESTIONS";
    }

    @Override
    public Result<Void, List<DomainError>> execute(UpdateQuestionnairePipelineContext context) {
        var existing = context.existingQuestionnaire();
        var command = context.command();

        Map<String, ConfiguredQuestion> byQuestionId = existing.configuredQuestions().stream()
                .collect(Collectors.toMap(
                        configuredQuestion -> configuredQuestion.question().id(),
                        configuredQuestion -> configuredQuestion,
                        (left, right) -> right,
                        LinkedHashMap::new
                ));

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
        return validateConditionRefs(finalQuestions)
                .map(__ -> {
                    context.configuredQuestions(finalQuestions);
                    return null;
                });
    }

    private Result<ConfiguredQuestion, List<DomainError>> assembleConfiguredQuestion(UpdateConfiguredQuestionParam upsert) {
        return Guard.requireNonNull(upsert, QuestionnaireErrors.INVALID_COMMAND)
                .flatMap(__ -> Guard.requireNonBlank(upsert.questionId(), QuestionnaireErrors.INVALID_ID))
                .flatMap(__ -> Guard.requireNonNull(upsert.param(), QuestionnaireErrors.INVALID_COMMAND))
                .flatMap(__ -> fetchQuestion(upsert.questionId()))
                .flatMap(question -> ConfiguredQuestionAssembler.build(question, upsert.param()));
    }

    private Result<Question, List<DomainError>> fetchQuestion(String questionId) {
        return questionCommandOutPort.findQuestionById(questionId)
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

    private <T> List<T> safeList(List<T> values) {
        return values == null ? List.of() : values;
    }
}

