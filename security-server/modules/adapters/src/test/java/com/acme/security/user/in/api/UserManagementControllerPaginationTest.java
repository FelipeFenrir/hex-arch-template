package com.acme.security.user.in.api;

import com.acme.security.common.api.ApiCollectionResponse;
import com.acme.security.common.api.ApiMeta;
import com.acme.security.user.dto.view.UserView;
import com.acme.security.user.in.dto.request.SearchUserRequest;
import com.acme.security.user.in.dto.response.UserResponse;
import com.acme.security.user.port.in.usecase.DeleteUserUseCase;
import com.acme.security.user.port.in.usecase.FindUserUseCase;
import com.acme.security.user.port.in.usecase.ListUsersUseCase;
import com.acme.security.user.port.in.usecase.RegisterUserUseCase;
import com.acme.security.user.port.in.usecase.UpdateUserUseCase;
import com.acme.shared.TenantContextHolder;
import com.acme.shared.engine.pagination.HybridPageRequest;
import com.acme.shared.engine.pagination.PageMode;
import com.acme.shared.engine.pagination.PageResult;
import com.acme.shared.engine.pagination.SortDirection;
import com.acme.shared.engine.pagination.SortSpec;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserManagementControllerPaginationTest {

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void shouldReturnPageModeCollectionWithMetaAndLinks() throws Exception {
        ListUsersUseCase listUsersUseCase = mock(ListUsersUseCase.class);
        when(listUsersUseCase.listByTenant(eq("tenant-a"), any(HybridPageRequest.class)))
                .thenReturn(PageResult.forPage(
                        List.of(new UserView("u1", "tenant-a", "alice", Set.of("ROLE_USER"), true)),
                        0,
                        2,
                        3,
                        2,
                        true,
                        false,
                        List.of(new SortSpec("username", SortDirection.ASC))
                ));

        UserManagementController controller = new UserManagementController(
                mock(RegisterUserUseCase.class),
                mock(FindUserUseCase.class),
                listUsersUseCase,
                mock(UpdateUserUseCase.class),
                mock(DeleteUserUseCase.class)
        );

        setCurrentRequest("/api/v1/users", "mode=PAGE&page=0&size=2&sort=username,ASC");

        TenantContextHolder.runWithTenant("tenant-a", () -> {
            ResponseEntity<ApiCollectionResponse<UserResponse>> response = controller.list(
                    new SearchUserRequest(PageMode.PAGE, 0, 2, null, List.of("username,ASC"))
            );

            assertEquals(200, response.getStatusCode().value());
            ApiCollectionResponse<UserResponse> body = response.getBody();
            assertNotNull(body);
            assertEquals(1, body.data().size());
            ApiMeta meta = body.meta();
            assertEquals("PAGE", meta.mode());
            assertEquals(0, meta.page());
            assertEquals(2, meta.size());
            assertEquals(3L, meta.totalItems());
            assertEquals(2, meta.totalPages());
            assertEquals(true, meta.hasNext());
            assertNull(meta.nextCursor());
            assertNotNull(body.links().next());
            assertEquals(null, body.links().previous());
        });

        ArgumentCaptor<HybridPageRequest> captor = ArgumentCaptor.forClass(HybridPageRequest.class);
        verify(listUsersUseCase).listByTenant(eq("tenant-a"), captor.capture());
        assertEquals(PageMode.PAGE, captor.getValue().mode());
        assertEquals(0, captor.getValue().page());
        assertEquals(2, captor.getValue().size());
        assertEquals(List.of(new SortSpec("username", SortDirection.ASC)), captor.getValue().sort());
    }

    @Test
    void shouldReturnCursorModeCollectionWithNextCursorLink() throws Exception {
        ListUsersUseCase listUsersUseCase = mock(ListUsersUseCase.class);
        when(listUsersUseCase.listByTenant(eq("tenant-a"), any(HybridPageRequest.class)))
                .thenReturn(PageResult.forCursor(
                        List.of(new UserView("u2", "tenant-a", "bob", Set.of("ROLE_USER"), true)),
                        1,
                        "2",
                        true,
                        List.of(new SortSpec("id", SortDirection.ASC))
                ));

        UserManagementController controller = new UserManagementController(
                mock(RegisterUserUseCase.class),
                mock(FindUserUseCase.class),
                listUsersUseCase,
                mock(UpdateUserUseCase.class),
                mock(DeleteUserUseCase.class)
        );

        setCurrentRequest("/api/v1/users", "mode=CURSOR&size=1");

        TenantContextHolder.runWithTenant("tenant-a", () -> {
            ResponseEntity<ApiCollectionResponse<UserResponse>> response = controller.list(
                    new SearchUserRequest(PageMode.CURSOR, null, 1, null, List.of("id,ASC"))
            );

            assertEquals(200, response.getStatusCode().value());
            ApiCollectionResponse<UserResponse> body = response.getBody();
            assertNotNull(body);
            assertEquals("CURSOR", body.meta().mode());
            assertEquals(null, body.meta().page());
            assertEquals("2", body.meta().nextCursor());
            assertNotNull(body.links().next());
            assertEquals(null, body.links().previous());
        });

        ArgumentCaptor<HybridPageRequest> captor = ArgumentCaptor.forClass(HybridPageRequest.class);
        verify(listUsersUseCase).listByTenant(eq("tenant-a"), captor.capture());
        assertEquals(PageMode.CURSOR, captor.getValue().mode());
        assertEquals(1, captor.getValue().size());
        assertEquals(List.of(new SortSpec("id", SortDirection.ASC)), captor.getValue().sort());
    }

    private static void setCurrentRequest(String uri, String queryString) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", uri);
        request.setQueryString(queryString);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }
}


