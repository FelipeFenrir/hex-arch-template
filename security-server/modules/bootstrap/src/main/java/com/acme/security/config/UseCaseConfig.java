package com.acme.security.config;

import com.acme.security.client.port.in.usecase.FindClientUseCase;
import com.acme.security.client.port.in.usecase.ListClientsUseCase;
import com.acme.security.client.port.in.usecase.RegisterClientUseCase;
import com.acme.security.client.port.out.repository.ClientCommandOutPort;
import com.acme.security.client.service.FindClientService;
import com.acme.security.client.service.ListClientsService;
import com.acme.security.client.service.RegisterClientService;
import com.acme.security.security.port.out.engine.PasswordEncoderPort;
import com.acme.security.tenant.port.in.usecase.CreateTenantUseCase;
import com.acme.security.tenant.port.in.usecase.DeleteTenantUseCase;
import com.acme.security.tenant.port.in.usecase.FindTenantUseCase;
import com.acme.security.tenant.port.in.usecase.GetTenantByIdUseCase;
import com.acme.security.tenant.port.in.usecase.ListTenantsUseCase;
import com.acme.security.tenant.port.in.usecase.UpdateTenantUseCase;
import com.acme.security.tenant.port.out.repository.TenantCommandOutPort;
import com.acme.security.tenant.service.CreateTenantService;
import com.acme.security.tenant.service.DeleteTenantService;
import com.acme.security.tenant.service.FindTenantService;
import com.acme.security.tenant.service.GetTenantByIdService;
import com.acme.security.tenant.service.ListTenantsService;
import com.acme.security.tenant.service.UpdateTenantService;
import com.acme.security.user.port.in.usecase.AuthenticateUserUseCase;
import com.acme.security.user.port.in.usecase.DeleteUserUseCase;
import com.acme.security.user.port.in.usecase.FindUserUseCase;
import com.acme.security.user.port.in.usecase.ListUsersUseCase;
import com.acme.security.user.port.in.usecase.RegisterUserUseCase;
import com.acme.security.user.port.in.usecase.UpdateUserUseCase;
import com.acme.security.user.port.out.repository.UserCommandOutPort;
import com.acme.security.user.service.AuthenticateUserService;
import com.acme.security.user.service.DeleteUserService;
import com.acme.security.user.service.FindUserService;
import com.acme.security.user.service.ListUsersService;
import com.acme.security.user.service.RegisterUserService;
import com.acme.security.user.service.UpdateUserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {
    // --- TENANT USE CASES ---
    @Bean
    public FindTenantUseCase findTenantUseCase(TenantCommandOutPort tenantCommandOutPort) {
        return new FindTenantService(tenantCommandOutPort);
    }

    @Bean
    public CreateTenantUseCase createTenantUseCase(TenantCommandOutPort tenantCommandOutPort) {
        return new CreateTenantService(tenantCommandOutPort);
    }

    @Bean
    public ListTenantsUseCase listTenantsUseCase(TenantCommandOutPort tenantCommandOutPort) {
        return new ListTenantsService(tenantCommandOutPort);
    }

    @Bean
    public GetTenantByIdUseCase getTenantByIdUseCase(TenantCommandOutPort tenantCommandOutPort) {
        return new GetTenantByIdService(tenantCommandOutPort);
    }

    @Bean
    public UpdateTenantUseCase updateTenantUseCase(TenantCommandOutPort tenantCommandOutPort) {
        return new UpdateTenantService(tenantCommandOutPort);
    }

    @Bean
    public DeleteTenantUseCase deleteTenantUseCase(TenantCommandOutPort tenantCommandOutPort) {
        return new DeleteTenantService(tenantCommandOutPort);
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

    @Bean
    public FindUserUseCase findUserUseCase(UserCommandOutPort userCommandOutPort) {
        return new FindUserService(userCommandOutPort);
    }

    @Bean
    public ListUsersUseCase listUsersUseCase(UserCommandOutPort userCommandOutPort) {
        return new ListUsersService(userCommandOutPort);
    }

    @Bean
    public UpdateUserUseCase updateUserUseCase(UserCommandOutPort userCommandOutPort,
                                               PasswordEncoderPort passwordEncoderPort) {
        return new UpdateUserService(userCommandOutPort, passwordEncoderPort);
    }

    @Bean
    public DeleteUserUseCase deleteUserUseCase(UserCommandOutPort userCommandOutPort) {
        return new DeleteUserService(userCommandOutPort);
    }

    // --- CLIENT USE CASES ---
    @Bean
    public FindClientUseCase findClientUseCase(ClientCommandOutPort clientCommandOutPort) {
        return new FindClientService(clientCommandOutPort);
    }

    @Bean
    public ListClientsUseCase listClientsUseCase(ClientCommandOutPort clientCommandOutPort) {
        return new ListClientsService(clientCommandOutPort);
    }

    @Bean
    public RegisterClientUseCase registerClientUseCase(ClientCommandOutPort clientCommandOutPort,
                                                       PasswordEncoderPort passwordEncoderPort) {
        return new RegisterClientService(clientCommandOutPort, passwordEncoderPort);
    }
}
