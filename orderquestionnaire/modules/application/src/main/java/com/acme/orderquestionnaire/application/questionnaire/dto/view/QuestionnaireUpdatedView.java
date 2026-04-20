package com.acme.orderquestionnaire.application.questionnaire.dto.view;

import com.acme.orderquestionnaire.application.audit.dto.view.UserView;
import com.acme.orderquestionnaire.application.questionnaire.service.support.ConditionQuestionIdExtractor;
import com.acme.orderquestionnaire.domain.questionnaire.Questionnaire;

import java.time.LocalDateTime;
import java.util.List;

public record QuestionnaireUpdatedView(
        String id,
        String channelDistributionId,
        String journeyDistributionId,
        String description,
        String status,
        List<QuestionConfigurationDetailedView> configuredQuestions,
        UserView createdBy,
        LocalDateTime createdAt,
        UserView updatedBy,
        LocalDateTime updatedAt
) {
    public static QuestionnaireUpdatedView from(Questionnaire questionnaire) {
        return new QuestionnaireUpdatedView(
                questionnaire.id(),
                questionnaire.channelDistributionId(),
                questionnaire.journeyDistributionId(),
                questionnaire.description(),
                questionnaire.status().name(),
                questionnaire.configuredQuestions().stream()
                        .map(configuredQuestion -> new QuestionConfigurationDetailedView(
                                configuredQuestion.question().id(),
                                configuredQuestion.order(),
                                AnswerConfigurationView.from(configuredQuestion.answerConfiguration()),
                                QuestionConditionView.from(configuredQuestion.rootCondition()),
                                ConditionQuestionIdExtractor.extract(configuredQuestion.rootCondition()).stream().toList()
                        ))
                        .toList(),
                UserView.from(questionnaire.auditInfo().createdBy()),
                questionnaire.auditInfo().createdAt(),
                UserView.from(questionnaire.auditInfo().updatedBy()),
                questionnaire.auditInfo().updatedAt()
        );
    }

    public record QuestionConfigurationDetailedView(
            String questionId,
            Integer order,
            AnswerConfigurationView answerConfiguration,
            QuestionConditionView rootCondition,
            List<String> dependsOnQuestionIds
    ) {
    }
}

