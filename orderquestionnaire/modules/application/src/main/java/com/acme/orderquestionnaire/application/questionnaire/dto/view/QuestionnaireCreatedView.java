package com.acme.orderquestionnaire.application.questionnaire.dto.view;

import com.acme.orderquestionnaire.application.audit.dto.view.UserView;
import com.acme.orderquestionnaire.domain.questionnaire.Questionnaire;

import java.time.LocalDateTime;

public record QuestionnaireCreatedView(
        String id,
        String channelDistributionId,
        String journeyDistributionId,
        String description,
        String status,
        UserView createdBy,
        LocalDateTime createdAt,
        UserView updatedBy,
        LocalDateTime updatedAt
) {
    public static QuestionnaireCreatedView from(Questionnaire questionnaire) {
        return new QuestionnaireCreatedView(
                questionnaire.id(),
                questionnaire.channelDistributionId(),
                questionnaire.journeyDistributionId(),
                questionnaire.description(),
                questionnaire.status().name(),
                UserView.from(questionnaire.auditInfo().createdBy()),
                questionnaire.auditInfo().createdAt(),
                UserView.from(questionnaire.auditInfo().updatedBy()),
                questionnaire.auditInfo().updatedAt()
        );
    }
}
