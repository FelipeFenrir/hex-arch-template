package com.acme.orderquestionnaire.domain.questionnaire;

import com.acme.orderquestionnaire.domain.question.Question;
import com.acme.orderquestionnaire.domain.questionnaire.tree.ConfiguredQuestionTreeNode;
import com.acme.orderquestionnaire.domain.questionnaire.tree.QuestionnaireTree;
import com.acme.orderquestionnaire.domain.questionnaire.vo.QuestionnaireId;
import com.acme.shared.enumerator.ParameterizationStatus;
import com.acme.shared.pattern.result.Result;
import com.acme.shared.vo.AuditInfo;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ToString
@EqualsAndHashCode
public final class Questionnaire {
    private final QuestionnaireId questionnaireId;
    private final String description;
    private final ParameterizationStatus status;
    private final List<ConfiguredQuestion> configuredQuestions;
    private final AuditInfo auditInfo;

    private Questionnaire(final QuestionnaireId questionnaireId,
                          final String description,
                          ParameterizationStatus status,
                          final List<ConfiguredQuestion> configuredQuestions,
                          final AuditInfo auditInfo) {
        this.questionnaireId = questionnaireId;
        this.description = description;
        this.status = status;
        this.auditInfo = auditInfo;
        this.configuredQuestions = configuredQuestions == null
                ? new ArrayList<>()
                : new ArrayList<>(configuredQuestions);
    }

    public static Questionnaire createNew(final QuestionnaireId questionnaireId,
                                          final String description,
                                          final AuditInfo auditInfo) {
        return new Questionnaire(questionnaireId,
                description, ParameterizationStatus.DRAFT, new ArrayList<>(), auditInfo);
    }

    public static Questionnaire createNew(final String id,
                                          final String channelDistributionId,
                                          final String journeyDistributionId,
                                          final String description,
                                          final AuditInfo auditInfo) {
        return createNew(QuestionnaireId.of(id, channelDistributionId, journeyDistributionId), description, auditInfo);
    }

    public static Questionnaire rehydrate(final QuestionnaireId questionnaireId,
                                          final String description,
                                          ParameterizationStatus status,
                                          final List<ConfiguredQuestion> configuredQuestions,
                                          final AuditInfo auditInfo) {
        return new Questionnaire(questionnaireId,
                description, status, configuredQuestions, auditInfo);
    }

    public static Questionnaire rehydrate(final QuestionnaireId questionnaireId,
                                          final String description,
                                          ParameterizationStatus status,
                                          final AuditInfo auditInfo) {
        return rehydrate(questionnaireId, description, status, new ArrayList<>(), auditInfo);
    }

    public static Questionnaire rehydrate(final String id,
                                          final String channelDistributionId,
                                          final String journeyDistributionId,
                                          final String description,
                                          ParameterizationStatus status,
                                          final List<ConfiguredQuestion> configuredQuestions,
                                          final AuditInfo auditInfo) {
        return rehydrate(QuestionnaireId.of(id, channelDistributionId, journeyDistributionId),
                description, status, configuredQuestions, auditInfo);
    }

    public static Questionnaire rehydrate(final String id,
                                          final String channelDistributionId,
                                          final String journeyDistributionId,
                                          final String description,
                                          ParameterizationStatus status,
                                          final AuditInfo auditInfo) {
        return rehydrate(id, channelDistributionId, journeyDistributionId,
                description, status, new ArrayList<>(), auditInfo);
    }

    public void addQuestion(ConfiguredQuestion question) {
        this.configuredQuestions.add(question);
    }

    public void addQuestions(List<ConfiguredQuestion> questions) {
        this.configuredQuestions.addAll(questions);
    }

    public void addQuestions(ConfiguredQuestion... questions) {
        this.configuredQuestions.addAll(List.of(questions));
    }

    public void removeQuestion(String questionId) {
        this.configuredQuestions.removeIf(
                configuredQuestion -> configuredQuestion.question().id().equals(questionId)
        );
    }

    public boolean isActive() {
        return this.status == ParameterizationStatus.ACTIVE;
    }

    public boolean canBeDeleted() {
        return this.status == ParameterizationStatus.DRAFT
                || this.status == ParameterizationStatus.INACTIVE;
    }

    public List<Question> getOrderedQuestions() {
        return configuredQuestions.stream()
                .sorted(Comparator.comparingInt(ConfiguredQuestion::order))
                .map(ConfiguredQuestion::question)
                .toList();
    }

    public List<ConfiguredQuestion> configuredQuestions() {
        return List.copyOf(configuredQuestions);
    }

    public boolean isReadyToActivate() {
        return !configuredQuestions.isEmpty()
                && configuredQuestions.stream().allMatch(configuredQuestion ->
                configuredQuestion != null
                        && configuredQuestion.answerConfiguration() != null
                        && configuredQuestion.order() >= 0
        );
    }

    public Result<Void, List<QuestionValidationFailure>> answerValidation(Map<String, Object> answers) {
        Map<String, ParameterizationStatus> questionStatuses = configuredQuestions.stream()
                .collect(Collectors.toMap(
                        cq -> cq.question().id(),
                        cq -> cq.question().status(),
                        (existing, replacement) -> existing
                ));

        List<QuestionValidationFailure> allFailures = new ArrayList<>();

        configuredQuestions.stream()
                .sorted(Comparator.comparingInt(ConfiguredQuestion::order))
                .map(item -> item.validate(answers, questionStatuses))
                .forEach(result -> result.onFailure(allFailures::add));

        return allFailures.isEmpty() ? Result.success(null) : Result.failure(allFailures);
    }

    public QuestionnaireTree toTree() {
        List<ConfiguredQuestionTreeNode> questionNodes = configuredQuestions.stream()
                .sorted(Comparator.comparingInt(ConfiguredQuestion::order))
                .map(ConfiguredQuestion::toTreeNode)
                .toList();

        return new QuestionnaireTree(
                questionnaireId.id(),
                questionnaireId.getChannelDistributionIdValue(),
                questionnaireId.getJourneyDistributionIdValue(),
                description,
                status.name(),
                questionNodes
        );
    }

    public QuestionnaireId questionnaireId() {
        return questionnaireId;
    }

    public String id() {
        return questionnaireId.id();
    }

    public String channelDistributionId() {
        return questionnaireId.getChannelDistributionIdValue();
    }

    public String journeyDistributionId() {
        return questionnaireId.getJourneyDistributionIdValue();
    }

    public String description() {
        return description;
    }

    public ParameterizationStatus status() {
        return status;
    }

    public AuditInfo auditInfo() {
        return auditInfo;
    }
}
