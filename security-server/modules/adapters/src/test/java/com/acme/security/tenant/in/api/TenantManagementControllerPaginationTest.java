package com.acme.security.tenant.in.api;

import com.acme.security.common.api.ApiCollectionResponse;
import com.acme.security.common.api.ApiMeta;
import com.acme.security.tenant.dto.view.TenantView;
import com.acme.security.tenant.in.dto.request.SearchTenantRequest;
import com.acme.security.tenant.in.dto.response.TenantResponse;
import com.acme.security.tenant.port.in.usecase.CreateTenantUseCase;
import com.acme.security.tenant.port.in.usecase.DeleteTenantUseCase;
import com.acme.security.tenant.port.in.usecase.GetTenantByIdUseCase;
import com.acme.security.tenant.port.in.usecase.ListTenantsUseCase;
import com.acme.security.tenant.port.in.usecase.UpdateTenantUseCase;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TenantManagementControllerPaginationTest {

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void shouldReturnPageModeCollectionWithMetaAndLinks() {
        ListTenantsUseCase listTenantsUseCase = mock(ListTenantsUseCase.class);
        when(listTenantsUseCase.list(any(HybridPageRequest.class)))
                .thenReturn(PageResult.forPage(
                        List.of(new TenantView("t1", "Tenant 1", "tenant-1", true)),
                        0,
                        2,
                        4,
                        2,
                        true,
                        false,
                        List.of(new SortSpec("name", SortDirection.ASC))
                ));

        TenantManagementController controller = new TenantManagementController(
                mock(CreateTenantUseCase.class),
                listTenantsUseCase,
                mock(GetTenantByIdUseCase.class),
                mock(UpdateTenantUseCase.class),
                mock(DeleteTenantUseCase.class)
        );

        setCurrentRequest("/api/v1/tenants", "mode=PAGE&page=0&size=2&sort=name,ASC");

        ResponseEntity<ApiCollectionResponse<TenantResponse>> response = controller.list(
                new SearchTenantRequest(PageMode.PAGE, 0, 2, null, List.of("name,ASC"))
        );

        assertEquals(200, response.getStatusCode().value());
        ApiCollectionResponse<TenantResponse> body = response.getBody();
        assertNotNull(body);
        assertEquals(1, body.data().size());
        ApiMeta meta = body.meta();
        assertEquals("PAGE", meta.mode());
        assertEquals(0, meta.page());
        assertEquals(2, meta.size());
        assertEquals(4L, meta.totalItems());
        assertEquals(2, meta.totalPages());
        assertEquals(true, meta.hasNext());
        assertNotNull(body.links().next());
        assertEquals(null, body.links().previous());

        ArgumentCaptor<HybridPageRequest> captor = ArgumentCaptor.forClass(HybridPageRequest.class);
        verify(listTenantsUseCase).list(captor.capture());
        assertEquals(PageMode.PAGE, captor.getValue().mode());
        assertEquals(0, captor.getValue().page());
        assertEquals(2, captor.getValue().size());
    }

    @Test
    void shouldReturnCursorModeCollectionWithCursorMeta() {
        ListTenantsUseCase listTenantsUseCase = mock(ListTenantsUseCase.class);
        when(listTenantsUseCase.list(any(HybridPageRequest.class)))
                .thenReturn(PageResult.forCursor(
                        List.of(new TenantView("t2", "Tenant 2", "tenant-2", true)),
                        1,
                        "2",
                        true,
                        List.of(new SortSpec("id", SortDirection.ASC))
                ));

        TenantManagementController controller = new TenantManagementController(
                mock(CreateTenantUseCase.class),
                listTenantsUseCase,
                mock(GetTenantByIdUseCase.class),
                mock(UpdateTenantUseCase.class),
                mock(DeleteTenantUseCase.class)
        );

        setCurrentRequest("/api/v1/tenants", "mode=CURSOR&size=1");

        ResponseEntity<ApiCollectionResponse<TenantResponse>> response = controller.list(
                new SearchTenantRequest(PageMode.CURSOR, null, 1, null, List.of("id,ASC"))
        );

        assertEquals(200, response.getStatusCode().value());
        ApiCollectionResponse<TenantResponse> body = response.getBody();
        assertNotNull(body);
        assertEquals("CURSOR", body.meta().mode());
        assertNull(body.meta().page());
        assertEquals("2", body.meta().nextCursor());
        assertNotNull(body.links().next());
        assertNull(body.links().previous());

        ArgumentCaptor<HybridPageRequest> captor = ArgumentCaptor.forClass(HybridPageRequest.class);
        verify(listTenantsUseCase).list(captor.capture());
        assertEquals(PageMode.CURSOR, captor.getValue().mode());
    }

    private static void setCurrentRequest(String uri, String queryString) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", uri);
        request.setQueryString(queryString);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }
}

