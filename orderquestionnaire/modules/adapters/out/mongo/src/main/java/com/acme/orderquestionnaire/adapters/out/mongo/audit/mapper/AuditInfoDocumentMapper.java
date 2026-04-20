package com.acme.orderquestionnaire.adapters.out.mongo.audit.mapper;

import com.acme.orderquestionnaire.adapters.out.mongo.audit.entity.AuditInfoDocument;
import com.acme.orderquestionnaire.adapters.out.mongo.audit.entity.AuditUserDocument;
import com.acme.orderquestionnaire.domain.audit.OrderQuestionnaireAuditInfo;
import com.acme.orderquestionnaire.domain.audit.OrderQuestionnaireAuditUser;
import com.acme.shared.vo.AuditInfo;
import org.springframework.stereotype.Component;

@Component
public class AuditInfoDocumentMapper {

    private final AuditUserDocumentMapper auditUserDocumentMapper;

    public AuditInfoDocumentMapper(AuditUserDocumentMapper auditUserDocumentMapper) {
        this.auditUserDocumentMapper = auditUserDocumentMapper;
    }

    public AuditInfoDocument toDocument(AuditInfo auditInfo) {
        AuditUserDocument updatedByDoc = auditInfo.updatedBy() != null
                ? auditUserDocumentMapper.toDocument(auditInfo.updatedBy())
                : null;

        return new AuditInfoDocument(
                auditUserDocumentMapper.toDocument(auditInfo.createdBy()),
                auditInfo.createdAt(),
                updatedByDoc,
                auditInfo.updatedAt()
        );
    }

    public AuditInfo toDomain(AuditInfoDocument doc) {
        OrderQuestionnaireAuditUser updatedBy = doc.updatedBy() != null
                ? auditUserDocumentMapper.toDomain(doc.updatedBy())
                : null;

        return new OrderQuestionnaireAuditInfo(
                auditUserDocumentMapper.toDomain(doc.createdBy()),
                doc.createdAt(),
                updatedBy,
                doc.updatedAt()
        );
    }
}

