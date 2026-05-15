package com.acme.security.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configurações seguras para bootstrap local de dados de teste/desenvolvimento.
 * NUNCA deve estar habilitado em produção.
 * <p>
 * Usa o prefixo 'security.bootstrap' para manter clareza e separação de configurações.
 */
@Component
@ConfigurationProperties(prefix = "security.bootstrap")
public class LocalBootstrapProperties {

    /**
     * Flag de habilitação do bootstrap local (desabilitado por padrão).
     * Só deve estar true no perfil 'local'.
     */
    private boolean enabled = false;

    /**
     * Ambiente esperado (SEGURANÇA: impede execução acidental em prod).
     * Valor padrão é "local". Se não bater com o perfil ativo, o bootstrap não executa.
     */
    private String environment = "local";

    /**
     * ID do tenant padrão a criar (se não existir).
     */
    private String tenantId = "default-tenant";

    /**
     * Nome amigável do tenant.
     */
    private String tenantName = "Default Tenant";

    /**
     * Slug (subdomínio) do tenant para URLs.
     */
    private String tenantSlug = "default";

    /**
     * Username do usuário admin a criar (se não existir).
     */
    private String adminUsername = "admin";

    /**
     * Senha raw do usuário admin (será codificada com Argon2).
     */
    private String adminPassword = "admin@123";

    /**
     * Client ID do cliente OAuth2 admin a criar (se não existir).
     */
    private String adminClientId = "admin-client";

    /**
     * Secret raw do cliente OAuth2 (será codificado com Argon2).
     */
    private String adminClientSecret = "admin-secret@123";

    // --- Getters and Setters ---
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getTenantName() {
        return tenantName;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    public String getTenantSlug() {
        return tenantSlug;
    }

    public void setTenantSlug(String tenantSlug) {
        this.tenantSlug = tenantSlug;
    }

    public String getAdminUsername() {
        return adminUsername;
    }

    public void setAdminUsername(String adminUsername) {
        this.adminUsername = adminUsername;
    }

    public String getAdminPassword() {
        return adminPassword;
    }

    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
    }

    public String getAdminClientId() {
        return adminClientId;
    }

    public void setAdminClientId(String adminClientId) {
        this.adminClientId = adminClientId;
    }

    public String getAdminClientSecret() {
        return adminClientSecret;
    }

    public void setAdminClientSecret(String adminClientSecret) {
        this.adminClientSecret = adminClientSecret;
    }
}

