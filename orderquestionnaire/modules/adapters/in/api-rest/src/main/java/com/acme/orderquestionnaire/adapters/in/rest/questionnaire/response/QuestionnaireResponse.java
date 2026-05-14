package com.acme.orderquestionnaire.adapters.in.rest.questionnaire.response;

import com.acme.orderquestionnaire.adapters.in.rest.audit.response.AuditUserResponse;
import com.acme.orderquestionnaire.application.questionnaire.dto.view.AnswerConfigurationView;
import com.acme.orderquestionnaire.application.questionnaire.dto.view.QuestionConditionView;
import com.acme.orderquestionnaire.application.questionnaire.dto.view.QuestionConfigurationView;
import com.acme.orderquestionnaire.application.questionnaire.dto.view.QuestionnaireCreatedView;
import com.acme.orderquestionnaire.application.questionnaire.dto.view.QuestionnaireUpdatedView;
import com.acme.orderquestionnaire.application.questionnaire.dto.view.QuestionnaireView;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public record QuestionnaireResponse(
        String id,
        String channelDistributionId,
        String journeyDistributionId,
        String description,
        String status,
        List<ConfiguredQuestionResponse> configuredQuestions,
        AuditUserResponse createdBy,
        LocalDateTime createdAt,
        AuditUserResponse updatedBy,
        LocalDateTime updatedAt
) {
    public static QuestionnaireResponse from(QuestionnaireCreatedView view) {
        return new QuestionnaireResponse(
                view.id(),
                view.channelDistributionId(),
                view.journeyDistributionId(),
                view.description(),
                view.status(),
                List.of(),
                AuditUserResponse.from(view.createdBy()),
                view.createdAt(),
                AuditUserResponse.from(view.updatedBy()),
                view.updatedAt()
        );
    }

    public static QuestionnaireResponse from(QuestionnaireUpdatedView view) {
        List<ConfiguredQuestionResponse> configuredQuestions = view.configuredQuestions().stream()
                .map(config -> new ConfiguredQuestionResponse(
                        config.questionId(),
                        config.order(),
                        AnswerConfigResponse.from(config.answerConfiguration()),
                        ConditionResponse.from(config.rootCondition()),
                        config.dependsOnQuestionIds() == null ? List.of() : List.copyOf(config.dependsOnQuestionIds())
                ))
                .toList();

        return new QuestionnaireResponse(
                view.id(),
                view.channelDistributionId(),
                view.journeyDistributionId(),
                view.description(),
                view.status(),
                configuredQuestions,
                AuditUserResponse.from(view.createdBy()),
                view.createdAt(),
                AuditUserResponse.from(view.updatedBy()),
                view.updatedAt()
        );
    }

    public static QuestionnaireResponse from(QuestionnaireView view) {
        List<ConfiguredQuestionResponse> configuredQuestions = view.configuredQuestions().stream()
                .map(QuestionnaireResponse::toConfiguredQuestion)
                .toList();

        return new QuestionnaireResponse(
                view.questionnaireId().id(),
                view.questionnaireId().getChannelDistributionIdValue(),
                view.questionnaireId().getJourneyDistributionIdValue(),
                view.description(),
                view.status(),
                configuredQuestions,
                AuditUserResponse.from(view.createdBy()),
                view.createdAt(),
                AuditUserResponse.from(view.updatedBy()),
                view.updatedAt()
        );
    }

    private static ConfiguredQuestionResponse toConfiguredQuestion(QuestionConfigurationView config) {
        ConditionResponse root = ConditionResponse.from(config.rootCondition());
        return new ConfiguredQuestionResponse(
                config.question() == null ? null : config.question().id(),
                config.order(),
                AnswerConfigResponse.from(config.answerConfiguration()),
                root,
                extractDependsOn(root)
        );
    }

    private static List<String> extractDependsOn(ConditionResponse condition) {
        if (condition == null) {
            return List.of();
        }

        Set<String> references = new LinkedHashSet<>();
        collectQuestionReferences(condition, references);
        return List.copyOf(references);
    }

    private static void collectQuestionReferences(ConditionResponse condition, Set<String> references) {
        if (condition.attributes() != null) {
            Object ref = condition.attributes().get("questionRootCode");
            if (ref instanceof String questionId && !questionId.isBlank()) {
                references.add(questionId);
            }
        }

        if (condition.children() != null) {
            for (ConditionResponse child : condition.children()) {
                collectQuestionReferences(child, references);
            }
        }
    }

    public record ConfiguredQuestionResponse(
            String questionId,
            Integer order,
            AnswerConfigResponse answerConfiguration,
            ConditionResponse rootCondition,
            List<String> dependsOnQuestionIds
    ) {
    }

    public record AnswerConfigResponse(
            String type,
            java.util.Map<String, Object> attributes
    ) {
        public static AnswerConfigResponse from(AnswerConfigurationView view) {
            return view == null ? null : new AnswerConfigResponse(view.type(), view.attributes());
        }
    }

    public record ConditionResponse(
            String type,
            java.util.Map<String, Object> attributes,
            List<ConditionResponse> children
    ) {
        public static ConditionResponse from(QuestionConditionView view) {
            if (view == null) {
                return null;
            }

            List<ConditionResponse> mappedChildren = view.children() == null
                    ? List.of()
                    : view.children().stream().map(ConditionResponse::from).toList();

            return new ConditionResponse(view.type(), view.attributes(), mappedChildren);
        }
    }
}

