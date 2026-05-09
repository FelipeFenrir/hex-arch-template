package com.acme.orderquestionnaire.adapters.in.rest.unit.audit.request;

import com.acme.orderquestionnaire.adapters.in.rest.audit.request.AuditUserRequest;
import com.acme.shared.stereotypes.test.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@UnitTest
@DisplayName("AuditUserRequest")
class AuditUserRequestTest {

    @Test
    @DisplayName("should convert string id into shared Id when creating command params")
    void shouldConvertStringIdIntoSharedIdWhenCreatingCommandParams() {
        AuditUserRequest request = new AuditUserRequest(
                "019dff07-5f02-70d4-8680-f8dc34fd5fb9",
                "seed_user",
                "Seed User",
                "seed.user@acme.com"
        );

        var param = request.toParam();

        assertEquals("019dff07-5f02-70d4-8680-f8dc34fd5fb9", param.id().stringfyId());
        assertEquals("seed_user", param.referenceCode());
    }
}

