package com.acme.orderquestionnaire.adapters.out.mongo.audit.mapper;

import com.acme.orderquestionnaire.adapters.out.mongo.audit.entity.AuditUserDocument;
import com.acme.orderquestionnaire.domain.audit.OrderQuestionnaireAuditUser;
import com.acme.shared.vo.AuditUser;
import com.acme.shared.vo.Id;
import com.acme.shared.vo.AuditReferenceCode;
import com.acme.shared.vo.EmailAddress;
import org.springframework.stereotype.Component;

@Component
public class AuditUserDocumentMapper {

    public AuditUserDocument toDocument(AuditUser user) {
        return new AuditUserDocument(
                user.id().stringfyId(),
                user.referenceCode(),
                user.name(),
                user.email()
        );
    }

    public OrderQuestionnaireAuditUser toDomain(AuditUserDocument doc) {
        return new OrderQuestionnaireAuditUser(
                Id.withId(doc.id()),
                AuditReferenceCode.of(doc.referenceCode()),
                doc.name(),
                toOptionalEmailAddress(doc.email())
        );
    }

    private EmailAddress toOptionalEmailAddress(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        try {
            return EmailAddress.of(email);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}

