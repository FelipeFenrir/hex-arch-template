package com.acme.orderquestionnaire.application.audit.dto.command;

import com.acme.shared.vo.Id;

public record AuditUserParam(
        Id id,
        String referenceCode,
        String name,
        String email
) { }

