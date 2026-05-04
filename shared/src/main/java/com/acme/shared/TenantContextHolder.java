package com.acme.shared;

import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;

import java.util.Objects;
import java.util.function.Supplier;

public final class TenantContextHolder {

    private TenantContextHolder() {
        throw new IllegalStateException("Utility class");
    }

    public static final ScopedValue<String> CURRENT_TENANT = ScopedValue.newInstance();

    public static String currentTenantOrNull() {
        return CURRENT_TENANT.isBound() ? CURRENT_TENANT.get() : null;
    }

    public static String currentTenant() {
        return currentTenantOrDefault("public");
    }

    public static String currentTenantOrDefault(String fallbackTenant) {
        return CURRENT_TENANT.isBound() ? CURRENT_TENANT.get() : fallbackTenant;
    }

    public static Result<String, DomainError> currentTenantRequired(Supplier<DomainError> missingTenantErrorSupplier) {
        Objects.requireNonNull(missingTenantErrorSupplier, "missingTenantErrorSupplier must not be null");
        return CURRENT_TENANT.isBound()
                ? Result.success(CURRENT_TENANT.get())
                : Result.failure(missingTenantErrorSupplier.get());
    }

    public static <X extends Throwable> void runWithTenant(String tenant, ThrowingRunnable<X> action) throws X {
        Objects.requireNonNull(tenant, "tenant must not be null");
        Objects.requireNonNull(action, "action must not be null");
        ScopedValue.where(CURRENT_TENANT, tenant).call(() -> {
            action.run();
            return null;
        });
    }

    @FunctionalInterface
    public interface ThrowingRunnable<X extends Throwable> {
        void run() throws X;
    }
}

