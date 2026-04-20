package com.acme.orderquestionnaire.application.question.dto.view;

import com.acme.orderquestionnaire.application.audit.dto.view.UserView;
import com.acme.orderquestionnaire.domain.question.Question;

import java.time.LocalDateTime;

public record QuestionUpdatedView(
        String id,
        String label,
        String status,
        String salesItemReferenceCode,
        UserView createdBy,
        LocalDateTime createdAt,
        UserView updatedBy,
        LocalDateTime updatedAt
) {
    public static QuestionUpdatedView from(Question question) {
        return new QuestionUpdatedView(
                question.id(),
                question.label(),
                question.status().name(),
                question.salesItemReferenceCode(),
                UserView.from(question.auditInfo().createdBy()),
                question.auditInfo().createdAt(),
                UserView.from(question.auditInfo().updatedBy()),
                question.auditInfo().updatedAt()
        );
    }
}
