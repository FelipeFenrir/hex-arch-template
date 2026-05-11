package com.acme.security.oauth.in.server;

import com.acme.security.tenant.Tenant;
import com.acme.security.tenant.erros.TenantDomainErrors;
import com.acme.security.tenant.port.in.usecase.FindTenantUseCase;
import com.acme.shared.constants.HeaderConstants;
import com.acme.shared.observability.CorrelationContext;
import com.acme.shared.pattern.result.Result;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TenantIdentifierFilterTest {

    @AfterEach
    void tearDown() {
        CorrelationContext.clear();
    }

    @Test
    void shouldPopulateMdcAndEchoHeadersForValidTenantRequest() throws Exception {
        FindTenantUseCase findTenantUseCase = mock(FindTenantUseCase.class);
        when(findTenantUseCase.findBySlug("tenant-a"))
                .thenReturn(Result.success(Tenant.createNew(UUID.randomUUID().toString(), "Tenant A", "tenant-a")));

        TenantIdentifierFilter filter = new TenantIdentifierFilter(findTenantUseCase);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/login");
        request.setServerName("tenant-a.127.0.0.1.nip.io");
        request.addHeader(HeaderConstants.CORRELATION_HEADER, "corr-123");
        request.addHeader(HeaderConstants.FLOW_HEADER, "login_flow");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        doAnswer(invocation -> {
            assertNotNull(invocation);
            assertEquals("corr-123", CorrelationContext.correlationId());
            assertEquals("login_flow", CorrelationContext.flowId());
            assertEquals("corr-123", response.getHeader(HeaderConstants.CORRELATION_HEADER));
            assertEquals("login_flow", response.getHeader(HeaderConstants.FLOW_HEADER));
            return null;
        }).when(chain).doFilter(any(), any());

        filter.doFilter(request, response, chain);

        verify(findTenantUseCase).findBySlug("tenant-a");
        assertNull(CorrelationContext.correlationId());
        assertNull(CorrelationContext.flowId());
    }

    @Test
    void shouldGenerateCorrelationIdWhenHeaderIsMissing() throws Exception {
        FindTenantUseCase findTenantUseCase = mock(FindTenantUseCase.class);
        when(findTenantUseCase.findBySlug("tenant-b"))
                .thenReturn(Result.success(Tenant.createNew(UUID.randomUUID().toString(), "Tenant B", "tenant-b")));

        TenantIdentifierFilter filter = new TenantIdentifierFilter(findTenantUseCase);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/register");
        request.setServerName("tenant-b.127.0.0.1.nip.io");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        doAnswer(invocation -> {
            assertNotNull(invocation);
            String correlationId = CorrelationContext.correlationId();
            assertNotNull(correlationId);
            assertFalse(correlationId.isBlank());
            assertEquals(correlationId, response.getHeader(HeaderConstants.CORRELATION_HEADER));
            assertNull(CorrelationContext.flowId());
            return null;
        }).when(chain).doFilter(any(), any());

        filter.doFilter(request, response, chain);

        verify(findTenantUseCase).findBySlug("tenant-b");
        assertNull(CorrelationContext.correlationId());
        assertNull(CorrelationContext.flowId());
    }

    @Test
    void shouldReturnBadRequestAndKeepCorrelationHeaderWhenTenantContextIsMissing() throws Exception {
        FindTenantUseCase findTenantUseCase = mock(FindTenantUseCase.class);
        TenantIdentifierFilter filter = new TenantIdentifierFilter(findTenantUseCase);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/login");
        request.setServerName("localhost");
        request.addHeader(HeaderConstants.CORRELATION_HEADER, "corr-400");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertEquals(400, response.getStatus());
        assertEquals(TenantDomainErrors.tenantContextMissing().message(), response.getContentAsString());
        assertEquals("corr-400", response.getHeader(HeaderConstants.CORRELATION_HEADER));
        verify(findTenantUseCase, never()).findBySlug(any());
        assertNull(CorrelationContext.correlationId());
        assertNull(CorrelationContext.flowId());
    }

    @Test
    void shouldReturnNotFoundWhenTenantLookupFails() throws Exception {
        FindTenantUseCase findTenantUseCase = mock(FindTenantUseCase.class);
        when(findTenantUseCase.findBySlug("tenant-c"))
                .thenReturn(Result.failure(TenantDomainErrors.tenantNotFound()));

        TenantIdentifierFilter filter = new TenantIdentifierFilter(findTenantUseCase);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/login");
        request.setServerName("tenant-c.127.0.0.1.nip.io");
        request.addHeader(HeaderConstants.CORRELATION_HEADER, "corr-404");
        request.addHeader(HeaderConstants.FLOW_HEADER, "oauth_login");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertEquals(404, response.getStatus());
        assertEquals(TenantDomainErrors.tenantNotFound().message(), response.getContentAsString());
        assertEquals("corr-404", response.getHeader(HeaderConstants.CORRELATION_HEADER));
        assertEquals("oauth_login", response.getHeader(HeaderConstants.FLOW_HEADER));
        verify(chain, never()).doFilter(any(), any());
        assertNull(CorrelationContext.correlationId());
        assertNull(CorrelationContext.flowId());
    }
}

