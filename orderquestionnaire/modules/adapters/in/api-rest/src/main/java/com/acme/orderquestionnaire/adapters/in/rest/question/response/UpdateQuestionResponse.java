package com.acme.orderquestionnaire.adapters.in.rest.question.response;

import com.acme.orderquestionnaire.adapters.in.rest.audit.response.AuditUserResponse;
import com.acme.orderquestionnaire.application.question.dto.view.QuestionUpdatedView;

import java.time.LocalDateTime;

public record UpdateQuestionResponse(
        String id,
        String label,
        String status,
        String salesItemReferenceCode,
        AuditUserResponse updatedBy,
        LocalDateTime updatedAt
) {
    public static UpdateQuestionResponse from(QuestionUpdatedView view) {
        return new UpdateQuestionResponse(
                view.id(),
                view.label(),
                view.status(),
                view.salesItemReferenceCode(),
                AuditUserResponse.from(view.updatedBy()),
                view.updatedAt()
        );
    }
}

