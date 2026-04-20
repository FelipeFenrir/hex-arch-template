package com.acme.orderquestionnaire.domain.questionnaire;

import com.acme.shared.vo.AuditInfo;
import com.acme.shared.vo.AuditUser;

import java.time.LocalDateTime;

public record QuestionnaireStatusTransitionContext(
        Questionnaire questionnaire,
        AuditUser actor,
        LocalDateTime occurredAt,
        AuditInfo auditInfo,
        boolean statusChanged
) {

    public QuestionnaireStatusTransitionContext withAuditInfo(AuditInfo newAuditInfo) {
        return new QuestionnaireStatusTransitionContext(questionnaire, actor, occurredAt, newAuditInfo, statusChanged);
    }

    public QuestionnaireStatusTransitionContext withStatusChanged(boolean value) {
        return new QuestionnaireStatusTransitionContext(questionnaire, actor, occurredAt, auditInfo, value);
    }
}

