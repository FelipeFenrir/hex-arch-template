package com.acme.orderquestionnaire.adapters.out.mongo.unit.audit.mapper;

import com.acme.orderquestionnaire.adapters.out.mongo.audit.mapper.AuditUserDocumentMapper;
import com.acme.orderquestionnaire.adapters.out.mongo.unit.support.MongoTestDataFactory;
import com.acme.shared.stereotypes.test.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@UnitTest
@DisplayName("AuditUserDocumentMapper")
class AuditUserDocumentMapperTest {

    private final AuditUserDocumentMapper mapper = new AuditUserDocumentMapper();

    // ── Mapping tests ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("should map audit user to document")
    void shouldMapAuditUserToDocument() {
        var user = MongoTestDataFactory.createdByUser();

        var doc = mapper.toDocument(user);

        assertEquals(user.id().stringfyId(), doc.id());
        assertEquals(user.referenceCode(), doc.referenceCode());
        assertEquals(user.name(), doc.name());
        assertEquals(user.email(), doc.email());
    }

    @Test
    @DisplayName("should map document to audit user")
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

