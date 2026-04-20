package com.acme.orderquestionnaire.application.questionnaire.dto.command;

import com.acme.orderquestionnaire.application.audit.dto.command.AuditUserParam;

import java.time.LocalDateTime;

public record CreateQuestionnaireCommand(
    String id,
    String channelDistributionId,
    String journeyDistributionId,
    String description,
    AuditUserParam createdBy,
    LocalDateTime createdAt
) { }
