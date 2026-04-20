package com.acme.orderquestionnaire.application.questionnaire.dto.queries;

import com.acme.orderquestionnaire.application.audit.dto.queries.SearchByAuditInfo;
import com.acme.shared.enumerator.ParameterizationStatus;
import com.acme.shared.engine.pagination.HybridPageRequest;

import java.util.List;

public record SearchQuestionnaireByFilter(
        List<String> ids,
        List<String> channelIds,
        List<String> journeyIds,
        String descriptionContains,
        ParameterizationStatus status,
        SearchByAuditInfo auditInfo,
        HybridPageRequest pageRequest
) { }