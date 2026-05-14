package com.acme.orderquestionnaire.domain.questionnaire;

import com.acme.orderquestionnaire.domain.questionnaire.errors.QuestionnaireDomainErrors;
import com.acme.orderquestionnaire.domain.questionnaire.vo.QuestionOrder;
import com.acme.orderquestionnaire.domain.question.Question;
import com.acme.orderquestionnaire.domain.questionnaire.answer.AnswerConfiguration;
import com.acme.orderquestionnaire.domain.questionnaire.conditioner.QuestionCondition;
import com.acme.orderquestionnaire.domain.questionnaire.tree.ConfiguredQuestionTreeNode;
import com.acme.orderquestionnaire.domain.questionnaire.tree.QuestionTreeNode;
import com.acme.shared.enumerator.ParameterizationStatus;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;
import com.acme.shared.vo.QuestionId;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@ToString
@EqualsAndHashCode
public class ConfiguredQuestion {
    private final Question question;
    private final AnswerConfiguration answerConfiguration;
    private QuestionOrder order;
    private QuestionCondition rootCondition;

    private ConfiguredQuestion(Question question, AnswerConfiguration answerConfiguration, int order) {
        this.question = question;
        this.answerConfiguration = answerConfiguration;
        this.order = QuestionOrder.of(order);
    }

    public static ConfiguredQuestion createNew(Question question,
                                               AnswerConfiguration answerConfiguration,
                                               int order) {
        return new ConfiguredQuestion(question, answerConfiguration, order);
    }

    public boolean isVisible(Map<String, Object> answers) {
        if (rootCondition == null) return true;
        return rootCondition.isSatisfy(answers);
    }

    public Result<Void, QuestionValidationFailure> validate(Map<String, Object> answers) {
        return validate(answers, Map.of());
    }

    public Result<Void, QuestionValidationFailure> validate(Map<String, Object> answers,
                                                             Map<String, ParameterizationStatus> questionStatuses) {
        // 1. Question itself is not active
        if (!question.isActive()) {
            if (answers.containsKey(question.id())) {
                return Result.failure(buildFailure(List.of(
                        QuestionnaireDomainErrors.questionNotActive(question.label(), question.status().name()))));
            }
            return Result.success(null);
        }

        // 2. Condition references inactive questions
        if (rootCondition != null && !questionStatuses.isEmpty()) {
            Set<String> inactiveRefs = rootCondition.referencedQuestionIds().stream()
                    .map(QuestionId::value)
                    .filter(id -> {
                        ParameterizationStatus refStatus = questionStatuses.get(id);
                        return refStatus != null && refStatus != ParameterizationStatus.ACTIVE;
                    })
                    .collect(Collectors.toCollection(LinkedHashSet::new));

            if (!inactiveRefs.isEmpty()) {
                return Result.failure(buildFailure(List.of(
                        QuestionnaireDomainErrors.conditionReferencedQuestionNotActive(question.label(), inactiveRefs))));
            }
        }

        // 3. Condition visibility
        if (!isVisible(answers)) {
            if (answers.containsKey(question.id())) {
                return Result.failure(buildFailure(List.of(
                        QuestionnaireDomainErrors.answerNotAllowedByCondition(question.label()))));
            }
            return Result.success(null);
        }

        // 4. Mandatory answer
        Object value = answers.get(question.id());
        if (value == null) {
            return Result.failure(buildFailure(List.of(QuestionnaireDomainErrors.mandatoryAnswer(question.label()))));
        }

        // 5. Answer configuration validation
        Result<Void, List<DomainError>> validation = answerConfiguration.validate(value);
        return validation.fold(
                __ -> Result.success(null),
                errors -> Result.failure(buildFailure(errors))
        );
    }

    public ConfiguredQuestionTreeNode toTreeNode() {
        return new ConfiguredQuestionTreeNode(
                order.value(),
                new QuestionTreeNode(
                        question.id(),
                        question.label(),
                        question.status().name(),
                        question.salesItemCode().value()
                ),
                answerConfiguration.toTreeNode(),
                rootCondition == null ? null : rootCondition.toTreeNode()
        );
    }

    private QuestionValidationFailure buildFailure(List<DomainError> errors) {
        return new QuestionValidationFailure(question.id(), question.label(), order.value(), errors);
    }

    public Question question() {
        return question;
    }
    public AnswerConfiguration answerConfiguration() {
        return answerConfiguration;
    }

    /**
     * Returns the order as an int value for backward compatibility at boundaries.
     */
    public int order() {
        return order.value();
    }

    /**
     * Returns the order as a Value Object. Use this when working within the domain.
     */
    public QuestionOrder questionOrder() {
        return order;
    }

    public void order(int order) {
        this.order = QuestionOrder.of(order);
    }

    public void rootCondition(QuestionCondition rootCondition) {
        this.rootCondition = rootCondition;
    }
    public QuestionCondition rootCondition() {
        return rootCondition;
    }
}
