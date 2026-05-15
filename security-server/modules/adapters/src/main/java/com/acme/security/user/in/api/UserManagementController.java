package com.acme.security.user.in.api;

import com.acme.observability.Loggable;
import com.acme.security.common.api.ApiCollectionResponse;
import com.acme.security.common.api.ApiLinks;
import com.acme.security.common.api.ApiMeta;
import com.acme.security.tenant.erros.TenantDomainErrors;
import com.acme.security.user.dto.view.UserView;
import com.acme.security.user.in.dto.request.CreateUserRequest;
import com.acme.security.user.in.dto.request.SearchUserRequest;
import com.acme.security.user.in.dto.request.UpdateUserRequest;
import com.acme.security.user.in.dto.response.UserResponse;
import com.acme.security.user.port.in.usecase.DeleteUserUseCase;
import com.acme.security.user.port.in.usecase.FindUserUseCase;
import com.acme.security.user.port.in.usecase.ListUsersUseCase;
import com.acme.security.user.port.in.usecase.RegisterUserUseCase;
import com.acme.security.user.port.in.usecase.UpdateUserUseCase;
import com.acme.shared.TenantContextHolder;
import com.acme.shared.engine.pagination.PageMode;
import com.acme.shared.engine.pagination.PageResult;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;
import java.util.Objects;

@RestController
@Loggable
public class UserManagementController implements UserManagementApi {

    private final RegisterUserUseCase registerUserUseCase;
    private final FindUserUseCase findUserUseCase;
    private final ListUsersUseCase listUsersUseCase;
    private final UpdateUserUseCase updateUserUseCase;
    private final DeleteUserUseCase deleteUserUseCase;

    public UserManagementController(RegisterUserUseCase registerUserUseCase,
                                    FindUserUseCase findUserUseCase,
                                    ListUsersUseCase listUsersUseCase,
                                    UpdateUserUseCase updateUserUseCase,
                                    DeleteUserUseCase deleteUserUseCase) {
        this.registerUserUseCase = Objects.requireNonNull(registerUserUseCase,
                "registerUserUseCase must not be null");
        this.findUserUseCase = Objects.requireNonNull(findUserUseCase,
                "findUserUseCase must not be null");
        this.listUsersUseCase = Objects.requireNonNull(listUsersUseCase,
                "listUsersUseCase must not be null");
        this.updateUserUseCase = Objects.requireNonNull(updateUserUseCase,
                "updateUserUseCase must not be null");
        this.deleteUserUseCase = Objects.requireNonNull(deleteUserUseCase,
                "deleteUserUseCase must not be null");
    }

    @Override
    public ResponseEntity<?> create(CreateUserRequest request) {
        Result<String, DomainError> tenantResult = currentTenant();
        if (tenantResult.isFailure()) {
            return toErrorResponse(tenantResult.fold(ignored -> null, error -> error));
        }

        String tenantId = tenantResult.fold(value -> value, ignored -> null);
        var command = request.toCommand(tenantId);

        var result = registerUserUseCase.execute(command)
                .map(UserView::from)
                .map(UserResponse::from);
        if (result.isFailure()) {
            return toErrorResponse(result.fold(ignored -> null, error -> error));
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(result.fold(value -> value, ignored -> null));
    }

    @Override
    public ResponseEntity<?> findById(String id) {
        Result<String, DomainError> tenantResult = currentTenant();
        if (tenantResult.isFailure()) {
            return toErrorResponse(tenantResult.fold(ignored -> null, error -> error));
        }

        var result = findUserUseCase.findById(id, tenantResult.fold(value -> value, ignored -> null))
                .map(UserView::from)
                .map(UserResponse::from);
        if (result.isFailure()) {
            return toErrorResponse(result.fold(ignored -> null, error -> error));
        }

        return ResponseEntity.ok(result.fold(value -> value, ignored -> null));
    }

    @Override
    public ResponseEntity<ApiCollectionResponse<UserResponse>> list(SearchUserRequest request) {
        Result<String, DomainError> tenantResult = currentTenant();
        if (tenantResult.isFailure()) {
            return ResponseEntity.badRequest().build();
        }

        String tenantId = tenantResult.fold(value -> value, ignored -> null);
        PageResult<UserView> page = listUsersUseCase.listByTenant(tenantId, request.toPageRequest());
        List<UserResponse> data = page.content().stream().map(UserResponse::from).toList();

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
    public ResponseEntity<?> update(String id, UpdateUserRequest request) {
        Result<String, DomainError> tenantResult = currentTenant();
        if (tenantResult.isFailure()) {
            return toErrorResponse(tenantResult.fold(ignored -> null, error -> error));
        }

        String tenantId = tenantResult.fold(value -> value, ignored -> null);
        var command = request.toCommand(id, tenantId);

        var result = updateUserUseCase.execute(command)
                .map(UserView::from)
                .map(UserResponse::from);
        if (result.isFailure()) {
            return toErrorResponse(result.fold(ignored -> null, error -> error));
        }

        return ResponseEntity.ok(result.fold(value -> value, ignored -> null));
    }

    @Override
    public ResponseEntity<?> delete(String id) {
        Result<String, DomainError> tenantResult = currentTenant();
        if (tenantResult.isFailure()) {
            return toErrorResponse(tenantResult.fold(ignored -> null, error -> error));
        }

        var result = deleteUserUseCase.execute(id, tenantResult.fold(value -> value, ignored -> null));
        if (result.isFailure()) {
            return toErrorResponse(result.fold(ignored -> null, error -> error));
        }

        return ResponseEntity.noContent().build();
    }

    private static Result<String, DomainError> currentTenant() {
        return TenantContextHolder.currentTenantRequired(TenantDomainErrors::tenantContextMissing);
    }

    private static ResponseEntity<String> toErrorResponse(DomainError error) {
        if (error == null) {
            return ResponseEntity.badRequest().body("Unknown error.");
        }

        if ("USER_NOT_FOUND".equals(error.code())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error.message());
        }

        return ResponseEntity.badRequest().body(error.message());
    }

    private String nextPageLink(PageResult<UserView> page) {
        if (!page.hasNext() || page.page() == null) {
            return null;
        }
        return ServletUriComponentsBuilder.fromCurrentRequest()
                .replaceQueryParam("mode", PageMode.PAGE.name())
                .replaceQueryParam("page", page.page() + 1)
                .replaceQueryParam("cursor")
                .toUriString();
    }

    private String previousPageLink(PageResult<UserView> page) {
        if (page.page() == null || page.page() <= 0) {
            return null;
        }
        return ServletUriComponentsBuilder.fromCurrentRequest()
                .replaceQueryParam("mode", PageMode.PAGE.name())
                .replaceQueryParam("page", page.page() - 1)
                .replaceQueryParam("cursor")
                .toUriString();
    }

    private String nextCursorLink(PageResult<UserView> page) {
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

