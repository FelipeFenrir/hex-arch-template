package com.acme.orderquestionnaire.adapters.in.rest.question.response;

import com.acme.orderquestionnaire.adapters.in.rest.audit.response.AuditUserResponse;
import com.acme.orderquestionnaire.application.question.dto.view.QuestionCreatedView;

import java.time.LocalDateTime;

public record CreateQuestionResponse(
        String id,
        String label,
        String status,
        String salesItemReferenceCode,
        AuditUserResponse createdBy,
        LocalDateTime createdAt
) {
    public static CreateQuestionResponse from(QuestionCreatedView view) {
        return new CreateQuestionResponse(
                view.id(),
                view.label(),
                view.status(),
                view.salesItemReferenceCode(),
                AuditUserResponse.from(view.createdBy()),
                view.createdAt()
        );
    }
}
