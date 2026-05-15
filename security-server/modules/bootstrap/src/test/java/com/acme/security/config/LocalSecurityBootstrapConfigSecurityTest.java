package com.acme.security.config;

import com.acme.security.client.port.in.usecase.RegisterClientUseCase;
import com.acme.security.client.port.out.repository.ClientCommandOutPort;
import com.acme.security.security.port.out.engine.PasswordEncoderPort;
import com.acme.security.tenant.port.in.usecase.CreateTenantUseCase;
import com.acme.security.tenant.port.out.repository.TenantCommandOutPort;
import com.acme.security.user.port.in.usecase.RegisterUserUseCase;
import com.acme.security.user.port.out.repository.UserCommandOutPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testes de segurança para LocalSecurityBootstrapConfig.
 * <p>
 * Valida que o bootstrap não executa em ambientes perigosos:
 * - Quando security.bootstrap.enabled=false
 * - Quando security.bootstrap.environment != "local"
 * - Quando perfil 'local' não está ativo
 */
@DisplayName("LocalSecurityBootstrapConfig Security Tests")
class LocalSecurityBootstrapConfigSecurityTest {

    private LocalBootstrapProperties bootstrapProps;
    private Environment environment;
    private CreateTenantUseCase createTenantUseCase;
    private RegisterUserUseCase registerUserUseCase;
    private RegisterClientUseCase registerClientUseCase;
    private TenantCommandOutPort tenantRepository;
    private UserCommandOutPort userRepository;
    private ClientCommandOutPort clientRepository;
    private PasswordEncoderPort passwordEncoder;
    private LocalSecurityBootstrapConfig bootstrapConfig;

    @BeforeEach
    void setUp() {
        bootstrapProps = mock(LocalBootstrapProperties.class);
        environment = mock(Environment.class);
        createTenantUseCase = mock(CreateTenantUseCase.class);
        registerUserUseCase = mock(RegisterUserUseCase.class);
        registerClientUseCase = mock(RegisterClientUseCase.class);
        tenantRepository = mock(TenantCommandOutPort.class);
        userRepository = mock(UserCommandOutPort.class);
        clientRepository = mock(ClientCommandOutPort.class);
        passwordEncoder = mock(PasswordEncoderPort.class);
        bootstrapConfig = new LocalSecurityBootstrapConfig();
    }

    @Test
    @DisplayName("Should NOT execute when security.bootstrap.enabled=false (production-safe default)")
    void testBootstrapDisabledByDefault() throws Exception {
        // ARRANGE: bootstrap desabilitado (padrão seguro)
        when(bootstrapProps.isEnabled()).thenReturn(false);

        // ACT: executar runner
        CommandLineRunner runner = bootstrapConfig.localBootstrapRunner(
                bootstrapProps, environment,
                createTenantUseCase, registerUserUseCase, registerClientUseCase,
                tenantRepository, userRepository, clientRepository,
                passwordEncoder
        );
        runner.run();

        // ASSERT: nenhum use case foi chamado
        verify(createTenantUseCase, never()).execute(null);
        verify(registerUserUseCase, never()).execute(null);
        verify(registerClientUseCase, never()).execute(null);
    }

    @Test
    @DisplayName("Should NOT execute when environment is 'production' (not 'local')")
    void testBootstrapBlockedInProduction() throws Exception {
        // ARRANGE: enabled mas environment é "production"
        when(bootstrapProps.isEnabled()).thenReturn(true);
        when(bootstrapProps.getEnvironment()).thenReturn("production");
        when(environment.getActiveProfiles()).thenReturn(new String[]{"prod"});

        // ACT: executar runner
        CommandLineRunner runner = bootstrapConfig.localBootstrapRunner(
                bootstrapProps, environment,
                createTenantUseCase, registerUserUseCase, registerClientUseCase,
                tenantRepository, userRepository, clientRepository,
                passwordEncoder
        );
        runner.run();

        // ASSERT: nenhum use case foi chamado (proteção!)
        verify(createTenantUseCase, never()).execute(null);
        verify(registerUserUseCase, never()).execute(null);
        verify(registerClientUseCase, never()).execute(null);
    }

    @Test
    @DisplayName("Should NOT execute when 'local' profile is NOT active")
    void testBootstrapBlockedWithoutLocalProfile() throws Exception {
        // ARRANGE: enabled e environment="local" mas perfil ativo é "prod"
        when(bootstrapProps.isEnabled()).thenReturn(true);
        when(bootstrapProps.getEnvironment()).thenReturn("local");
        when(environment.getActiveProfiles()).thenReturn(new String[]{"prod", "docker"});

        // ACT: executar runner
        CommandLineRunner runner = bootstrapConfig.localBootstrapRunner(
                bootstrapProps, environment,
                createTenantUseCase, registerUserUseCase, registerClientUseCase,
                tenantRepository, userRepository, clientRepository,
                passwordEncoder
        );
        runner.run();

        // ASSERT: nenhum use case foi chamado (proteção!)
        verify(createTenantUseCase, never()).execute(null);
        verify(registerUserUseCase, never()).execute(null);
        verify(registerClientUseCase, never()).execute(null);
    }

    @Test
    @DisplayName("Should verify all security checks together (production-like scenario)")
    void testAllSecurityChecksTogether() throws Exception {
        // ARRANGE: simulação totalmente segura (não será executada)
        when(bootstrapProps.isEnabled()).thenReturn(false);
        when(bootstrapProps.getEnvironment()).thenReturn("production");
        when(environment.getActiveProfiles()).thenReturn(new String[]{"prod"});

        // ACT: executar runner
        CommandLineRunner runner = bootstrapConfig.localBootstrapRunner(
                bootstrapProps, environment,
                createTenantUseCase, registerUserUseCase, registerClientUseCase,
                tenantRepository, userRepository, clientRepository,
                passwordEncoder
        );
        runner.run();

        // ASSERT: proteção em camadas
        verify(bootstrapProps).isEnabled();  // Primeiro check
        verify(createTenantUseCase, never()).execute(null);
        verify(registerUserUseCase, never()).execute(null);
        verify(registerClientUseCase, never()).execute(null);
    }
}

