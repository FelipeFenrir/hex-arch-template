package com.acme.security.user;

import com.acme.security.user.errors.UserDomainErrors;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;
import com.acme.shared.vo.Id;
import com.acme.shared.vo.TenantId;

import java.util.Set;

public class User {
    private final Id id;
    private final TenantId tenantId; // Campo de Tenancy
    private final String username;
    private String password;
    private final Set<String> roles;
    private boolean active;

    // Construtor privado para controle total
    private User(Id id, TenantId tenantId, String username, String password, Set<String> roles, boolean active) {
        this.id = id;
        this.tenantId = tenantId;
        this.username = username;
        this.password = password;
        this.roles = roles;
        this.active = active;
    }

    // Para um novo usuário, geramos o ‘ID’ agora
    public static User createNew(TenantId tenantId, String username, String password, Set<String> roles) {
        return new User(Id.withoutId(), tenantId, username, password, roles, true);
    }

    // Para reconstruir do MongoDB, usamos o ID que veio de lá
    public static User rehydrate(String id, String tenantId, String username, String password, Set<String> roles,
                                 boolean active) {
        return new User(Id.withId(id), TenantId.fromString(tenantId), username, password, roles, active);
    }

    // Métodos de negócio em vez de Setters puros
    public Result<User, DomainError> changePassword(String newEncodedPassword) {
        if (newEncodedPassword == null || newEncodedPassword.isBlank()) {
            return Result.failure(UserDomainErrors.invalidPassword());
        }

        if (this.password.equals(newEncodedPassword)) {
            return Result.failure(UserDomainErrors.samePassword());
        }

        this.password = newEncodedPassword;
        return Result.success(this);
    }

    public void deactivate() {
        this.active = false;
    }

    public Id id() {
        return id;
    }
    public String idValue() {
        return this.id.stringfyId();
    }
    public TenantId tenantId() {
        return tenantId;
    }
    public String tenantValue() { return tenantId.stringValue(); }
    public String username() {
        return username;
    }
    public String password() {
        return password;
    }
    public Set<String> roles() {
        return roles;
    }
    public boolean isActive() {
        return active;
    }

}
