package com.acme.security.config;

import com.acme.security.client.port.in.usecase.FindClientUseCase;
import com.acme.security.client.port.in.usecase.RegisterClientUseCase;
import com.acme.security.client.port.out.repository.ClientCommandOutPort;
import com.acme.security.client.service.FindClientService;
import com.acme.security.client.service.RegisterClientService;
import com.acme.security.security.port.out.engine.PasswordEncoderPort;
import com.acme.security.tenant.port.in.usecase.FindTenantUseCase;
import com.acme.security.tenant.port.out.repository.TenantCommandOutPort;
import com.acme.security.tenant.service.FindTenantService;
import com.acme.security.user.port.in.usecase.AuthenticateUserUseCase;
import com.acme.security.user.port.in.usecase.RegisterUserUseCase;
import com.acme.security.user.port.out.repository.UserCommandOutPort;
import com.acme.security.user.service.AuthenticateUserService;
import com.acme.security.user.service.RegisterUserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {
    // --- TENANT USE CASES ---
    @Bean
    public FindTenantUseCase findTenantUseCase(TenantCommandOutPort tenantCommandOutPort) {
        return new FindTenantService(tenantCommandOutPort);
    }

    // --- USER USE CASES ---
    @Bean
    public RegisterUserUseCase registerUserUseCase(UserCommandOutPort userCommandOutPort,
                                                   PasswordEncoderPort passwordEncoderPort) {
        return new RegisterUserService(userCommandOutPort, passwordEncoderPort);
    }

    @Bean
    public AuthenticateUserUseCase authenticateUserUseCase(UserCommandOutPort userCommandOutPort) {
        return new AuthenticateUserService(userCommandOutPort);
    }

    // --- CLIENT USE CASES ---
    @Bean
    public FindClientUseCase findClientUseCase(ClientCommandOutPort clientCommandOutPort) {
        return new FindClientService(clientCommandOutPort);
    }

    @Bean
    public RegisterClientUseCase registerClientUseCase(ClientCommandOutPort clientCommandOutPort,
                                                       PasswordEncoderPort passwordEncoderPort) {
        return new RegisterClientService(clientCommandOutPort, passwordEncoderPort);
    }
}
