package com.acme.orderquestionnaire.domain.question;

import com.acme.shared.enumerator.ParameterizationStatus;
import com.acme.shared.vo.AuditInfo;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public final class Question {
    private final String id;
    private final String label;
    private final ParameterizationStatus status;
    private final String salesItemReferenceCode;
    private final AuditInfo auditInfo;

    private Question(final String id,
                     final String label,
                     final ParameterizationStatus status,
                     final String salesItemReferenceCode,
                     final AuditInfo auditInfo) {
        this.id = id;
        this.label = label;
        this.status = status;
        this.salesItemReferenceCode = salesItemReferenceCode;
        this.auditInfo = auditInfo;
    }

    public static Question createNew(final String id,
                                     final String label,
                                     final String salesItemReferenceCode,
                                     final AuditInfo auditInfo) {
        return new Question(id, label, ParameterizationStatus.DRAFT, salesItemReferenceCode, auditInfo);
    }

    public static Question rehydrate(final String id,
                              final String label,
                              final ParameterizationStatus status,
                              final String salesItemReferenceCode,
                              final AuditInfo auditInfo) {
        return new Question(id, label, status, salesItemReferenceCode, auditInfo);
    }

    public boolean isActive() {
        return this.status == ParameterizationStatus.ACTIVE;
    }

    public String id() {
        return id;
    }
    public String label() {
        return label;
    }
    public ParameterizationStatus status() {
        return status;
    }
    public String salesItemReferenceCode() {
        return salesItemReferenceCode;
    }

    public AuditInfo auditInfo() {
        return auditInfo;
    }
}
