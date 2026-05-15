package com.acme.security.config;

import com.acme.security.client.dto.command.ClientRegistrationCommand;
import com.acme.security.client.port.in.usecase.RegisterClientUseCase;
import com.acme.security.client.port.out.repository.ClientCommandOutPort;
import com.acme.security.security.port.out.engine.PasswordEncoderPort;
import com.acme.security.tenant.dto.command.CreateTenantCommand;
import com.acme.security.tenant.port.in.usecase.CreateTenantUseCase;
import com.acme.security.tenant.port.out.repository.TenantCommandOutPort;
import com.acme.security.user.dto.command.UserRegistrationCommand;
import com.acme.security.user.port.in.usecase.RegisterUserUseCase;
import com.acme.security.user.port.out.repository.UserCommandOutPort;
import com.acme.shared.vo.TenantId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

import java.util.Set;

/**
 * Bootstrap runner seguro e idempotente para ambiente local.
 * <p>
 * Executa apenas se:
 * 1. security.bootstrap.enabled=true (padrão: false)
 * 2. Perfil 'local' está ativo
 * 3. security.bootstrap.environment="local" (proteção anti-prod)
 * <p>
 * Idempotente: verifica se tenant/user/client já existem antes de criar.
 */
@Configuration
@Profile("local")
@Slf4j
public class LocalSecurityBootstrapConfig {

    @Bean
    public CommandLineRunner localBootstrapRunner(
            LocalBootstrapProperties bootstrapProps,
            Environment environment,
            CreateTenantUseCase createTenantUseCase,
            RegisterUserUseCase registerUserUseCase,
            RegisterClientUseCase registerClientUseCase,
            TenantCommandOutPort tenantRepository,
            UserCommandOutPort userRepository,
            ClientCommandOutPort clientRepository,
            PasswordEncoderPort passwordEncoder) {

        return args -> {
            // Proteção: verifica se bootstrap está habilitado
            if (!bootstrapProps.isEnabled()) {
                log.debug("Bootstrap local desabilitado (security.bootstrap.enabled=false)");
                return;
            }

            // Proteção: verifica ambiente esperado
            if (!bootstrapProps.getEnvironment().equals("local")) {
                log.error("SEGURANÇA VIOLADA: ambiente esperado é '{}' mas obteve '{}'. " +
                        "Bootstrap abortado para proteção.",
                        "local", bootstrapProps.getEnvironment());
                return;
            }

            // Proteção: verifica se perfil 'local' está ativo
            boolean hasLocalProfile = false;
            for (String profile : environment.getActiveProfiles()) {
                if ("local".equals(profile)) {
                    hasLocalProfile = true;
                    break;
                }
            }

            if (!hasLocalProfile) {
                log.error("SEGURANÇA VIOLADA: perfil 'local' não está ativo. " +
                        "Perfis ativos: {}. Bootstrap abortado para proteção.",
                        String.join(", ", environment.getActiveProfiles()));
                return;
            }

            log.info("Iniciando bootstrap seguro e idempotente para ambiente local...");

            // 1. Criar tenant (idempotente)
            TenantId tenantId = TenantId.fromString(bootstrapProps.getTenantId());
            if (tenantRepository.findBySlug(bootstrapProps.getTenantSlug()).isEmpty()) {
                log.info("Criando tenant padrão: {} (slug: {})",
                        bootstrapProps.getTenantName(),
                        bootstrapProps.getTenantSlug());

                CreateTenantCommand createTenantCmd = new CreateTenantCommand(
                        bootstrapProps.getTenantId(),
                        bootstrapProps.getTenantName(),
                        bootstrapProps.getTenantSlug()
                );

                var createResult = createTenantUseCase.execute(createTenantCmd);
                if (createResult.isFailure()) {
                    createResult.onFailure(error ->
                        log.error("Falha ao criar tenant: {}", error.message())
                    );
                    return;
                }
                log.info("Tenant criado com sucesso: {}", bootstrapProps.getTenantId());
            } else {
                log.info("Tenant já existe: {} (slug: {})",
                        bootstrapProps.getTenantName(),
                        bootstrapProps.getTenantSlug());
            }

            // 2. Criar usuário admin (idempotente)
            if (userRepository.findByUsernameAndTenant(bootstrapProps.getAdminUsername(), tenantId).isEmpty()) {
                log.info("Criando usuário admin: {}", bootstrapProps.getAdminUsername());

                String encodedPassword = passwordEncoder.encode(bootstrapProps.getAdminPassword());

                UserRegistrationCommand userRegCmd = new UserRegistrationCommand(
                        bootstrapProps.getAdminUsername(),
                        encodedPassword,
                        "admin@local.test",
                        bootstrapProps.getTenantId(),
                        Set.of("ROLE_ADMIN")
                );

                var userResult = registerUserUseCase.execute(userRegCmd);
                if (userResult.isFailure()) {
                    userResult.onFailure(error ->
                        log.error("Falha ao criar usuário admin: {}", error.message())
                    );
                    return;
                }
                log.info("Usuário admin criado com sucesso: {}", bootstrapProps.getAdminUsername());
            } else {
                log.info("Usuário admin já existe: {}", bootstrapProps.getAdminUsername());
            }

            // 3. Criar cliente OAuth2 admin (idempotente)
            if (clientRepository.findByClientIdAndTenant(bootstrapProps.getAdminClientId(), tenantId).isEmpty()) {
                log.info("Criando cliente OAuth2 admin: {}", bootstrapProps.getAdminClientId());

                String encodedSecret = passwordEncoder.encode(bootstrapProps.getAdminClientSecret());

                ClientRegistrationCommand clientRegCmd = new ClientRegistrationCommand(
                        bootstrapProps.getTenantId(),
                        bootstrapProps.getAdminClientId(),
                        bootstrapProps.getAdminClientSecret(),
                        Set.of("http://localhost:3000/callback", "http://localhost:5000/callback"),
                        Set.of("openid", "profile", "email"),
                        Set.of("authorization_code", "refresh_token", "client_credentials")
                );

                var clientResult = registerClientUseCase.execute(clientRegCmd);
                if (clientResult.isFailure()) {
                    clientResult.onFailure(error ->
                        log.error("Falha ao criar cliente OAuth2: {}", error.message())
                    );
                    return;
                }
                log.info("Cliente OAuth2 criado com sucesso: {}", bootstrapProps.getAdminClientId());
            } else {
                log.info("Cliente OAuth2 admin já existe: {}", bootstrapProps.getAdminClientId());
            }

            log.info("Bootstrap local concluído com sucesso!");
        };
    }
}


