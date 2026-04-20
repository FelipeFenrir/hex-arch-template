package com.acme.security.tenant;

import com.acme.shared.vo.TenantId;

public class Tenant {
    private final TenantId id;
    private final String name;
    private final String slug; // O nome que aparece no subdomínio (ex: "cliente-a")
    private boolean active;

    private Tenant(TenantId id, String name, String slug, boolean active) {
        this.id = id;
        this.name = name;
        this.slug = slug;
        this.active = active;
    }

    public static Tenant createNew(String id, String name, String slug) {
        return new Tenant(TenantId.fromString(id), name, slug, true);
    }

    public static Tenant rehydrate(String id, String name, String slug, boolean active) {
        return new Tenant(TenantId.fromString(id), name, slug, active);
    }

    public String idValue() { return id.stringValue(); }
    public String slug() { return slug; }
    public String name() { return name; }
    public boolean isActive() { return active; }
}
