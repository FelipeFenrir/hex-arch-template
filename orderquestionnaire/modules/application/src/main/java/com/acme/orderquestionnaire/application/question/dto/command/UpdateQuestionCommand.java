package com.acme.orderquestionnaire.application.question.dto.command;

import com.acme.orderquestionnaire.application.audit.dto.command.AuditUserParam;
import com.acme.shared.enumerator.ParameterizationStatus;
import com.acme.shared.vo.SalesItemReferenceCode;

import java.time.LocalDateTime;

public record UpdateQuestionCommand(
        String label,
        String salesItemReferenceCode,
        ParameterizationStatus status,
        AuditUserParam updatedBy,
        LocalDateTime updatedAt
) {
    public SalesItemReferenceCode salesItemCode() {
        return SalesItemReferenceCode.of(salesItemReferenceCode);
    }
}
