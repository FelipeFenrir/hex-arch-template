package com.acme.orderquestionnaire.adapters.out.mongo.audit.mapper;

import com.acme.orderquestionnaire.adapters.out.mongo.support.MongoTestDataFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AuditUserDocumentMapperTest {

    private final AuditUserDocumentMapper mapper = new AuditUserDocumentMapper();

    @Test
    void shouldMapAuditUserToDocument() {
        var user = MongoTestDataFactory.createdByUser();

        var doc = mapper.toDocument(user);

        assertEquals(user.id().stringfyId(), doc.id());
        assertEquals(user.referenceCode(), doc.referenceCode());
        assertEquals(user.name(), doc.name());
        assertEquals(user.email(), doc.email());
    }

    @Test
    void shouldMapDocumentToAuditUser() {
        var user = MongoTestDataFactory.updatedByUser();
        var doc = mapper.toDocument(user);

        var mapped = mapper.toDomain(doc);

        assertEquals(doc.id(), mapped.id().stringfyId());
        assertEquals(doc.referenceCode(), mapped.referenceCode());
        assertEquals(doc.name(), mapped.name());
        assertEquals(doc.email(), mapped.email());
    }
}

