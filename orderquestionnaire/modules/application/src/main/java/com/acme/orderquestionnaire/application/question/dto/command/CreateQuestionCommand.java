package com.acme.orderquestionnaire.application.question.dto.command;

import com.acme.orderquestionnaire.application.audit.dto.command.AuditUserParam;
import com.acme.shared.vo.QuestionId;
import com.acme.shared.vo.SalesItemReferenceCode;

import java.time.LocalDateTime;

public record CreateQuestionCommand(
        String id,
        String label,
        String salesItemReferenceCode,
        AuditUserParam createdBy,
        LocalDateTime createdAt
) {
    public QuestionId questionId() {
        return QuestionId.of(id);
    }

    public SalesItemReferenceCode salesItemCode() {
        return SalesItemReferenceCode.of(salesItemReferenceCode);
    }
}
