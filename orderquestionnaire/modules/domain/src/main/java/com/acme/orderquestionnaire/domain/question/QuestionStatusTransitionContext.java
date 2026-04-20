package com.acme.orderquestionnaire.domain.question;

import com.acme.shared.vo.AuditInfo;
import com.acme.shared.vo.AuditUser;

import java.time.LocalDateTime;

public record QuestionStatusTransitionContext(
        Question question,
        AuditUser actor,
        LocalDateTime occurredAt,
        AuditInfo auditInfo,
        boolean statusChanged
) {

    public QuestionStatusTransitionContext withAuditInfo(AuditInfo newAuditInfo) {
        return new QuestionStatusTransitionContext(question, actor, occurredAt, newAuditInfo, statusChanged);
    }

    public QuestionStatusTransitionContext withStatusChanged(boolean value) {
        return new QuestionStatusTransitionContext(question, actor, occurredAt, auditInfo, value);
    }
}

