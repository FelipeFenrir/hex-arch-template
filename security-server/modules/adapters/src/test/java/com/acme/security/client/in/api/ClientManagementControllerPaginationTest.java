package com.acme.security.client.in.api;

import com.acme.security.client.dto.view.ClientView;
import com.acme.security.client.in.dto.request.SearchClientRequest;
import com.acme.security.client.in.dto.response.ClientResponse;
import com.acme.security.client.port.in.usecase.ListClientsUseCase;
import com.acme.security.client.port.in.usecase.RegisterClientUseCase;
import com.acme.security.common.api.ApiCollectionResponse;
import com.acme.security.common.api.ApiMeta;
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

class ClientManagementControllerPaginationTest {

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void shouldReturnPageModeCollectionWithMetaAndLinks() throws Exception {
        ListClientsUseCase listClientsUseCase = mock(ListClientsUseCase.class);
        when(listClientsUseCase.listByTenant(eq("tenant-a"), any(HybridPageRequest.class)))
                .thenReturn(PageResult.forPage(
                        List.of(new ClientView("c1", "tenant-a", "client-a", Set.of("http://localhost/cb"),
                                Set.of("openid"), Set.of("authorization_code"), true)),
                        0,
                        2,
                        3,
                        2,
                        true,
                        false,
                        List.of(new SortSpec("clientId", SortDirection.ASC))
                ));

        ClientManagementController controller = new ClientManagementController(
                mock(RegisterClientUseCase.class),
                listClientsUseCase
        );

        setCurrentRequest("/api/v1/clients", "mode=PAGE&page=0&size=2&sort=clientId,ASC");

        TenantContextHolder.runWithTenant("tenant-a", () -> {
            ResponseEntity<ApiCollectionResponse<ClientResponse>> response = controller.list(
                    new SearchClientRequest(PageMode.PAGE, 0, 2, null, List.of("clientId,ASC"))
            );

            assertEquals(200, response.getStatusCode().value());
            ApiCollectionResponse<ClientResponse> body = response.getBody();
            assertNotNull(body);
            assertEquals(1, body.data().size());
            ApiMeta meta = body.meta();
            assertEquals("PAGE", meta.mode());
            assertEquals(0, meta.page());
            assertEquals(2, meta.size());
            assertEquals(3L, meta.totalItems());
            assertEquals(2, meta.totalPages());
            assertEquals(true, meta.hasNext());
            assertNotNull(body.links().next());
            assertNull(body.links().previous());
        });

        ArgumentCaptor<HybridPageRequest> captor = ArgumentCaptor.forClass(HybridPageRequest.class);
        verify(listClientsUseCase).listByTenant(eq("tenant-a"), captor.capture());
        assertEquals(PageMode.PAGE, captor.getValue().mode());
    }

    @Test
    void shouldReturnCursorModeCollectionWithCursorMeta() throws Exception {
        ListClientsUseCase listClientsUseCase = mock(ListClientsUseCase.class);
        when(listClientsUseCase.listByTenant(eq("tenant-a"), any(HybridPageRequest.class)))
                .thenReturn(PageResult.forCursor(
                        List.of(new ClientView("c2", "tenant-a", "client-b", Set.of("http://localhost/cb"),
                                Set.of("openid"), Set.of("authorization_code"), true)),
                        1,
                        "2",
                        true,
                        List.of(new SortSpec("id", SortDirection.ASC))
                ));

        ClientManagementController controller = new ClientManagementController(
                mock(RegisterClientUseCase.class),
                listClientsUseCase
        );

        setCurrentRequest("/api/v1/clients", "mode=CURSOR&size=1");

        TenantContextHolder.runWithTenant("tenant-a", () -> {
            ResponseEntity<ApiCollectionResponse<ClientResponse>> response = controller.list(
                    new SearchClientRequest(PageMode.CURSOR, null, 1, null, List.of("id,ASC"))
            );

            assertEquals(200, response.getStatusCode().value());
            ApiCollectionResponse<ClientResponse> body = response.getBody();
            assertNotNull(body);
            assertEquals("CURSOR", body.meta().mode());
            assertNull(body.meta().page());
            assertEquals("2", body.meta().nextCursor());
            assertNotNull(body.links().next());
            assertNull(body.links().previous());
        });

        ArgumentCaptor<HybridPageRequest> captor = ArgumentCaptor.forClass(HybridPageRequest.class);
        verify(listClientsUseCase).listByTenant(eq("tenant-a"), captor.capture());
        assertEquals(PageMode.CURSOR, captor.getValue().mode());
    }

    private static void setCurrentRequest(String uri, String queryString) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", uri);
        request.setQueryString(queryString);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }
}

