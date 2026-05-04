package com.acme.orderquestionnaire.application.questionnaire.dto.view;

import com.acme.orderquestionnaire.application.audit.dto.view.UserView;
import com.acme.orderquestionnaire.domain.questionnaire.Questionnaire;
import com.acme.orderquestionnaire.domain.questionnaire.vo.QuestionnaireId;
import com.acme.shared.vo.AuditInfo;

import java.time.LocalDateTime;
import java.util.List;

public record QuestionnaireView(
        QuestionnaireId questionnaireId,
        String description,
        String status,
        List<QuestionConfigurationView>configuredQuestions,
        UserView createdBy,
        LocalDateTime createdAt,
        UserView updatedBy,
        LocalDateTime updatedAt
) {
    public static QuestionnaireView from(Questionnaire questionnaire) {
        AuditInfo auditInfo = questionnaire.auditInfo();

        return new QuestionnaireView(
                questionnaire.questionnaireId(),
                questionnaire.description(),
                questionnaire.status().name(),
                questionnaire.configuredQuestions().stream().map(QuestionConfigurationView::from).toList(),
                auditInfo == null ? null : UserView.from(auditInfo.createdBy()),
                auditInfo == null ? null : auditInfo.createdAt(),
                auditInfo == null ? null : UserView.from(auditInfo.updatedBy()),
                auditInfo == null ? null : auditInfo.updatedAt()
        );
    }
}
