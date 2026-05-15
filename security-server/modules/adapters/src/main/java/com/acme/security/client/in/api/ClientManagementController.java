package com.acme.security.client.in.api;

import com.acme.observability.Loggable;
import com.acme.security.common.api.ApiCollectionResponse;
import com.acme.security.common.api.ApiLinks;
import com.acme.security.common.api.ApiMeta;
import com.acme.security.client.dto.view.ClientView;
import com.acme.security.client.in.dto.request.SearchClientRequest;
import com.acme.security.client.in.dto.response.ClientResponse;
import com.acme.security.client.in.dto.request.CreateClientRequest;
import com.acme.security.client.port.in.usecase.ListClientsUseCase;
import com.acme.security.client.port.in.usecase.RegisterClientUseCase;
import com.acme.security.tenant.erros.TenantDomainErrors;
import com.acme.shared.TenantContextHolder;
import com.acme.shared.engine.pagination.PageMode;
import com.acme.shared.engine.pagination.PageResult;
import com.acme.shared.pattern.result.DomainError;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;
import java.util.Objects;

@RestController
@Loggable
public class ClientManagementController implements ClientManagementApi {

    private final RegisterClientUseCase registerClientUseCase;
    private final ListClientsUseCase listClientsUseCase;

    public ClientManagementController(RegisterClientUseCase registerClientUseCase,
                                      ListClientsUseCase listClientsUseCase) {
        this.registerClientUseCase = Objects.requireNonNull(registerClientUseCase,
                "registerClientUseCase must not be null");
        this.listClientsUseCase = Objects.requireNonNull(listClientsUseCase,
                "listClientsUseCase must not be null");
    }

    @Override
    public ResponseEntity<?> createClient(@RequestBody CreateClientRequest request) {
        var tenantResult = TenantContextHolder.currentTenantRequired(TenantDomainErrors::tenantContextMissing);
        if (tenantResult.isFailure()) {
            return ResponseEntity.badRequest().body(tenantResult.fold(ignored -> null, DomainError::message));
        }

        String tenantId = tenantResult.fold(value -> value, ignored -> null);
        var command = request.toCommand(tenantId);

        var result = registerClientUseCase.execute(command)
                .map(ClientView::from)
                .map(ClientResponse::from);

        if (result.isSuccess()) {
            return ResponseEntity.ok(result.fold(client -> client, ignored -> null));
        }
        return ResponseEntity.badRequest().body(result.fold(ignored -> null, DomainError::message));
    }

    @Override
    public ResponseEntity<ApiCollectionResponse<ClientResponse>> list(SearchClientRequest request) {
        var tenantResult = TenantContextHolder.currentTenantRequired(TenantDomainErrors::tenantContextMissing);
        if (tenantResult.isFailure()) {
            return ResponseEntity.badRequest().build();
        }

        String tenantId = tenantResult.fold(value -> value, ignored -> null);
        PageResult<ClientView> page = listClientsUseCase.listByTenant(tenantId, request.toPageRequest());
        List<ClientResponse> data = page.content().stream().map(ClientResponse::from).toList();

        ApiMeta meta = new ApiMeta(
                page.mode().name(),
                page.page(),
                page.size(),
                page.totalElements(),
                page.totalPages(),
                page.hasNext(),
                page.nextCursor()
        );

        ApiLinks links = page.mode() == PageMode.CURSOR
                ? new ApiLinks(nextCursorLink(page), null)
                : new ApiLinks(nextPageLink(page), previousPageLink(page));

        return ResponseEntity.ok(ApiCollectionResponse.of(data, meta, links));
    }

    private String nextPageLink(PageResult<ClientView> page) {
        if (!page.hasNext() || page.page() == null) {
            return null;
        }
        return ServletUriComponentsBuilder.fromCurrentRequest()
                .replaceQueryParam("mode", PageMode.PAGE.name())
                .replaceQueryParam("page", page.page() + 1)
                .replaceQueryParam("cursor")
                .toUriString();
    }

    private String previousPageLink(PageResult<ClientView> page) {
        if (page.page() == null || page.page() <= 0) {
            return null;
        }
        return ServletUriComponentsBuilder.fromCurrentRequest()
                .replaceQueryParam("mode", PageMode.PAGE.name())
                .replaceQueryParam("page", page.page() - 1)
                .replaceQueryParam("cursor")
                .toUriString();
    }

    private String nextCursorLink(PageResult<ClientView> page) {
        if (!page.hasNext() || page.nextCursor() == null || page.nextCursor().isBlank()) {
            return null;
        }
        return ServletUriComponentsBuilder.fromCurrentRequest()
                .replaceQueryParam("mode", PageMode.CURSOR.name())
                .replaceQueryParam("cursor", page.nextCursor())
                .replaceQueryParam("page")
                .toUriString();
    }
}
