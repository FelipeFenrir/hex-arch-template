package com.acme.orderquestionnaire.application.questionnaire.dto.view;

import com.acme.orderquestionnaire.application.audit.dto.view.UserView;
import com.acme.orderquestionnaire.domain.questionnaire.vo.QuestionnaireId;

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
}
