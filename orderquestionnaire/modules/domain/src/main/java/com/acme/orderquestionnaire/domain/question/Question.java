package com.acme.orderquestionnaire.domain.question;

import com.acme.shared.enumerator.ParameterizationStatus;
import com.acme.shared.vo.AuditInfo;
import com.acme.shared.vo.QuestionId;
import com.acme.shared.vo.SalesItemReferenceCode;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public final class Question {
    private final QuestionId id;
    private final String label;
    private final ParameterizationStatus status;
    private final SalesItemReferenceCode salesItemReferenceCode;
    private final AuditInfo auditInfo;

    private Question(final QuestionId id,
                     final String label,
                     final ParameterizationStatus status,
                     final SalesItemReferenceCode salesItemReferenceCode,
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
        return createNew(
                QuestionId.of(id),
                label,
                SalesItemReferenceCode.of(salesItemReferenceCode),
                auditInfo
        );
    }

    public static Question createNew(final QuestionId id,
                                     final String label,
                                     final SalesItemReferenceCode salesItemReferenceCode,
                                     final AuditInfo auditInfo) {
        return new Question(
                id,
                label,
                ParameterizationStatus.DRAFT,
                salesItemReferenceCode,
                auditInfo
        );
    }

    public static Question rehydrate(final String id,
                              final String label,
                              final ParameterizationStatus status,
                              final String salesItemReferenceCode,
                              final AuditInfo auditInfo) {
        return rehydrate(
                QuestionId.of(id),
                label,
                status,
                salesItemReferenceCode == null ? null : SalesItemReferenceCode.of(salesItemReferenceCode),
                auditInfo
        );
    }

    public static Question rehydrate(final QuestionId id,
                              final String label,
                              final ParameterizationStatus status,
                              final SalesItemReferenceCode salesItemReferenceCode,
                              final AuditInfo auditInfo) {
        return new Question(
                id,
                label,
                status,
                salesItemReferenceCode,
                auditInfo
        );
    }

    public boolean isActive() {
        return this.status == ParameterizationStatus.ACTIVE;
    }

    /**
     * Returns the question ID as a string value. Used for backward compatibility at boundaries.
     */
    public String id() {
        return id.value();
    }

    /**
     * Returns the question ID as a Value Object. Use this when working within the domain.
     */
    public QuestionId questionId() {
        return id;
    }

    public String label() {
        return label;
    }
    public ParameterizationStatus status() {
        return status;
    }

    /**
     * Returns the sales item reference code as a string value. Used for backward compatibility at boundaries.
     */
    public String salesItemReferenceCode() {
        return salesItemReferenceCode == null ? null : salesItemReferenceCode.value();
    }

    /**
     * Returns the sales item reference code as a Value Object. Use this when working within the domain.
     */
    public SalesItemReferenceCode salesItemCode() {
        return salesItemReferenceCode;
    }

    public AuditInfo auditInfo() {
        return auditInfo;
    }
}
