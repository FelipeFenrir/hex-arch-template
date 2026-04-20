package com.acme.orderquestionnaire.application.question.dto.command;

import com.acme.orderquestionnaire.application.audit.dto.command.AuditUserParam;

import java.time.LocalDateTime;

public record CreateQuestionCommand(
        String id,
        String label,
        String salesItemReferenceCode,
        AuditUserParam createdBy,
        LocalDateTime createdAt
) { }
