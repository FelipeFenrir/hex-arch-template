package com.acme.orderquestionnaire.adapters.in.rest.question.response;

import com.acme.orderquestionnaire.adapters.in.rest.audit.response.AuditUserResponse;
import com.acme.orderquestionnaire.application.question.dto.view.QuestionView;

import java.time.LocalDateTime;

public record QuestionResponse(
        String id,
        String label,
        String status,
        String salesItemReferenceCode,
        AuditUserResponse createdBy,
        LocalDateTime createdAt,
        AuditUserResponse updatedBy,
        LocalDateTime updatedAt
) {
    public static QuestionResponse from(QuestionView view) {
        return new QuestionResponse(
                view.id(),
                view.label(),
                view.status(),
                view.salesItemReferenceCode(),
                AuditUserResponse.from(view.createdBy()),
                view.createdAt(),
                AuditUserResponse.from(view.updatedBy()),
                view.updatedAt()
        );
    }
}

