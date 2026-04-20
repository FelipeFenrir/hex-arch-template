package com.acme.security.tenant.out.mongo.document;

import com.acme.security.tenant.Tenant;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "tenants")
public record TenantDocument(
        @Id
        String id,
        @Indexed(unique = true)
        String slug,
        String name,
        boolean active
) {
    public static TenantDocument of(Tenant tenant) {
        return new TenantDocument(
                tenant.idValue(),
                tenant.slug(),
                tenant.name(),
                tenant.isActive()
        );
    }

    public static Tenant map(TenantDocument tenantDocument) {
        return Tenant.rehydrate(
                tenantDocument.id(),
                tenantDocument.name(),
                tenantDocument.slug(),
                tenantDocument.active()
        );
    }
}
