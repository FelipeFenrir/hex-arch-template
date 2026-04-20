package com.acme.shared;

public final class TenantContextHolder {

    private TenantContextHolder() {
        throw new IllegalStateException("Utility class");
    }

    private static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();

    public static void setTenant(String tenant) {
        CURRENT_TENANT.set(tenant);
    }

    public static String currentTenant() {
        return CURRENT_TENANT.get();
    }

    public static void clear() {
        CURRENT_TENANT.remove();
    }
}

