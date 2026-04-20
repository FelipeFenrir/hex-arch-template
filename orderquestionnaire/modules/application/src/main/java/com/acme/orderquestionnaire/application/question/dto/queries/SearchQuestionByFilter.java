package com.acme.orderquestionnaire.application.question.dto.queries;

import com.acme.orderquestionnaire.application.audit.dto.queries.SearchByAuditInfo;
import com.acme.shared.enumerator.ParameterizationStatus;
import com.acme.shared.engine.pagination.HybridPageRequest;

import java.util.List;

public record SearchQuestionByFilter(
        List<String> ids,
        List<String> salesItemReferenceCodes,
        String labelContains,
        ParameterizationStatus status,
        SearchByAuditInfo auditInfo,
        HybridPageRequest pageRequest
) { }
