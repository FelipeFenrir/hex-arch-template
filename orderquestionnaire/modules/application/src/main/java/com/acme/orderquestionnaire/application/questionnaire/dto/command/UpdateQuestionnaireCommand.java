package com.acme.orderquestionnaire.application.questionnaire.dto.command;

import com.acme.orderquestionnaire.application.audit.dto.command.AuditUserParam;
import com.acme.shared.enumerator.ParameterizationStatus;

import java.time.LocalDateTime;
import java.util.List;

public record UpdateQuestionnaireCommand(
        String id,
        String channelDistributionId,
        String journeyDistributionId,
        String description,
        ParameterizationStatus status,
        List<UpdateConfiguredQuestionParam> questionsToUpsert,
        List<String> questionIdsToRemove,
        AuditUserParam updatedBy,
        LocalDateTime updatedAt
) { }

