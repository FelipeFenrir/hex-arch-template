package com.acme.security.tenant.in.api;

import com.acme.observability.Loggable;
import com.acme.security.common.api.ApiCollectionResponse;
import com.acme.security.common.api.ApiLinks;
import com.acme.security.common.api.ApiMeta;
import com.acme.security.tenant.dto.view.TenantView;
import com.acme.security.tenant.in.dto.request.CreateTenantRequest;
import com.acme.security.tenant.in.dto.request.SearchTenantRequest;
import com.acme.security.tenant.in.dto.request.UpdateTenantRequest;
import com.acme.security.tenant.in.dto.response.TenantResponse;
import com.acme.security.tenant.port.in.usecase.CreateTenantUseCase;
import com.acme.security.tenant.port.in.usecase.DeleteTenantUseCase;
import com.acme.security.tenant.port.in.usecase.GetTenantByIdUseCase;
import com.acme.security.tenant.port.in.usecase.ListTenantsUseCase;
import com.acme.security.tenant.port.in.usecase.UpdateTenantUseCase;
import com.acme.shared.engine.pagination.PageMode;
import com.acme.shared.engine.pagination.PageResult;
import com.acme.shared.pattern.result.DomainError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;
import java.util.Objects;

@RestController
@Loggable
public class TenantManagementController implements TenantManagementApi {

    private final CreateTenantUseCase createTenantUseCase;
    private final ListTenantsUseCase listTenantsUseCase;
    private final GetTenantByIdUseCase getTenantByIdUseCase;
    private final UpdateTenantUseCase updateTenantUseCase;
    private final DeleteTenantUseCase deleteTenantUseCase;

    public TenantManagementController(CreateTenantUseCase createTenantUseCase,
                                      ListTenantsUseCase listTenantsUseCase,
                                      GetTenantByIdUseCase getTenantByIdUseCase,
                                      UpdateTenantUseCase updateTenantUseCase,
                                      DeleteTenantUseCase deleteTenantUseCase) {
        this.createTenantUseCase = Objects.requireNonNull(createTenantUseCase,
                "createTenantUseCase must not be null");
        this.listTenantsUseCase = Objects.requireNonNull(listTenantsUseCase,
                "listTenantsUseCase must not be null");
        this.getTenantByIdUseCase = Objects.requireNonNull(getTenantByIdUseCase,
                "getTenantByIdUseCase must not be null");
        this.updateTenantUseCase = Objects.requireNonNull(updateTenantUseCase,
                "updateTenantUseCase must not be null");
        this.deleteTenantUseCase = Objects.requireNonNull(deleteTenantUseCase,
                "deleteTenantUseCase must not be null");
    }

    @Override
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> create(CreateTenantRequest request) {
        var result = createTenantUseCase.execute(request.toCommand())
                .map(TenantView::from)
                .map(TenantResponse::from);

        if (result.isFailure()) {
            return toErrorResponse(result.fold(ignored -> null, error -> error));
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(result.fold(value -> value, ignored -> null));
    }

    @Override
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_SUPER_ADMIN')")
    public ResponseEntity<ApiCollectionResponse<TenantResponse>> list(SearchTenantRequest request) {
        PageResult<TenantView> page = listTenantsUseCase.list(request.toPageRequest());
        List<TenantResponse> data = page.content().stream().map(TenantResponse::from).toList();

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

    @Override
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> getById(String id) {
        var result = getTenantByIdUseCase.execute(id)
                .map(TenantView::from)
                .map(TenantResponse::from);
        if (result.isFailure()) {
            return toErrorResponse(result.fold(ignored -> null, error -> error));
        }

        return ResponseEntity.ok(result.fold(value -> value, ignored -> null));
    }

    @Override
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> update(String id, UpdateTenantRequest request) {
        var result = updateTenantUseCase.execute(request.toCommand(id))
                .map(TenantView::from)
                .map(TenantResponse::from);

        if (result.isFailure()) {
            return toErrorResponse(result.fold(ignored -> null, error -> error));
        }

        return ResponseEntity.ok(result.fold(value -> value, ignored -> null));
    }

    @Override
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> delete(String id) {
        var result = deleteTenantUseCase.execute(id);
        if (result.isFailure()) {
            return toErrorResponse(result.fold(ignored -> null, error -> error));
        }

        return ResponseEntity.noContent().build();
    }

    private static ResponseEntity<String> toErrorResponse(DomainError error) {
        if (error == null) {
            return ResponseEntity.badRequest().body("Unknown error.");
        }

        if ("TENANT_NOT_FOUND".equals(error.code())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error.message());
        }

        return ResponseEntity.badRequest().body(error.message());
    }

    private String nextPageLink(PageResult<TenantView> page) {
        if (!page.hasNext() || page.page() == null) {
            return null;
        }
        return ServletUriComponentsBuilder.fromCurrentRequest()
                .replaceQueryParam("mode", PageMode.PAGE.name())
                .replaceQueryParam("page", page.page() + 1)
                .replaceQueryParam("cursor")
                .toUriString();
    }

    private String previousPageLink(PageResult<TenantView> page) {
        if (page.page() == null || page.page() <= 0) {
            return null;
        }
        return ServletUriComponentsBuilder.fromCurrentRequest()
                .replaceQueryParam("mode", PageMode.PAGE.name())
                .replaceQueryParam("page", page.page() - 1)
                .replaceQueryParam("cursor")
                .toUriString();
    }

    private String nextCursorLink(PageResult<TenantView> page) {
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



