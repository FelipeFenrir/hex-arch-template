package com.acme.security.client.in.api;

import com.acme.security.client.in.dto.request.CreateClientRequest;
import com.acme.security.client.dto.command.ClientRegistrationCommand;
import com.acme.security.client.port.in.usecase.RegisterClientUseCase;
import com.acme.security.tenant.erros.TenantDomainErrors;
import com.acme.shared.TenantContextHolder;
import com.acme.shared.pattern.result.DomainError;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/clients")
public class ClientManagementController {

    private final RegisterClientUseCase registerClientUseCase;

    public ClientManagementController(RegisterClientUseCase registerClientUseCase) {
        this.registerClientUseCase = registerClientUseCase;
    }

    @PostMapping
    public ResponseEntity<?> createClient(@RequestBody CreateClientRequest request) {
        var tenantResult = TenantContextHolder.currentTenantRequired(TenantDomainErrors::tenantContextMissing);
        if (tenantResult.isFailure()) {
            return ResponseEntity.badRequest().body(tenantResult.fold(ignored -> null, DomainError::message));
        }

        var command = new ClientRegistrationCommand(
                tenantResult.fold(value -> value, ignored -> null),
                request.clientId(),
                request.clientSecret(),
                Arrays.stream(request.redirectUris().split(",")).map(String::trim).collect(Collectors.toSet()),
                Arrays.stream(request.scopes().split(",")).map(String::trim).collect(Collectors.toSet()),
                Arrays.stream(request.grantTypes().split(",")).map(String::trim).collect(Collectors.toSet())
        );

        var result = registerClientUseCase.execute(command);

        if (result.isSuccess()) {
            return ResponseEntity.ok(result.fold(client -> client, ignored -> null));
        }
        return ResponseEntity.badRequest().body(result.fold(ignored -> null, DomainError::message));
    }
}
