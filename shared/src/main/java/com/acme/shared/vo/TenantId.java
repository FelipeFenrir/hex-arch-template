package com.acme.shared.vo;

public record TenantId(Id value) {
    public static TenantId fromString(String id) {
        return new TenantId(Id.withId(id));
    }
    public String stringValue() {
        return value.stringfyId();
    }
}