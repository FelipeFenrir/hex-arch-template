package com.acme.security.oauth.in.server;

import com.acme.security.tenant.port.in.usecase.FindTenantUseCase;
import com.acme.security.tenant.Tenant;
import com.acme.security.tenant.erros.TenantDomainErrors;
import com.acme.shared.TenantContextHolder;
import com.acme.observability.TraceContextPropagator;
import com.acme.shared.constants.HeaderConstants;
import com.acme.shared.observability.CorrelationContext;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;
import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

@Component
public class TenantIdentifierFilter extends OncePerRequestFilter {

    private static final String BASE_DOMAIN = ".127.0.0.1.nip.io";
    private final FindTenantUseCase findTenantUseCase;

    public TenantIdentifierFilter(FindTenantUseCase findTenantUseCase) {
        this.findTenantUseCase = Objects.requireNonNull(findTenantUseCase,
                "findTenantUseCase must not be null");
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        return requestUri != null
                && (requestUri.startsWith("/actuator")
                || requestUri.startsWith("/api/v1/tenants"));
    }

    @Override
    protected void doFilterInternal(@Nonnull HttpServletRequest request,
                                    @Nonnull HttpServletResponse response,
                                    @Nonnull FilterChain filterChain) throws ServletException, IOException {

        String correlationId = correlationIdOrGenerate(request);
        String flowId = request.getHeader(HeaderConstants.FLOW_HEADER);

        response.setHeader(HeaderConstants.CORRELATION_HEADER, correlationId);
        if (flowId != null && !flowId.isBlank()) {
            response.setHeader(HeaderConstants.FLOW_HEADER, flowId);
        }

        CorrelationContext.populate(correlationId, flowId);

        TraceContextPropagator.populateBaggageFromContext();
        TraceContextPropagator.extractTraceContextToMdc();

        try {
            String serverName = request.getServerName();

            if (serverName == null || !serverName.endsWith(BASE_DOMAIN)) {
                writeDomainError(response, TenantDomainErrors.tenantContextMissing());
                return;
            }

            String tenantSlug = serverName.replace(BASE_DOMAIN, "");
            if (tenantSlug.isBlank()) {
                writeDomainError(response, TenantDomainErrors.tenantContextMissing());
                return;
            }

            // Railway: Validando o Tenant via Use Case
            Result<Tenant, DomainError> result = findTenantUseCase.findBySlug(tenantSlug);

            if (result.isFailure()) {
                // Se falha (ex: tenant inativo), interrompe e retorna erro limpo
                String errorMessage = result.fold(ignored -> "", DomainError::message);
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write(errorMessage);
                return;
            }

            // Se sucesso, executa a cadeia dentro do escopo do tenant no ScopedValue
            String tenantId = result.fold(Tenant::idValue, ignored -> null);
            try {
                TenantContextHolder.runWithTenant(tenantId, () -> filterChain.doFilter(request, response));
            } catch (ServletException | IOException exception) {
                throw exception;
            } catch (Exception exception) {
                throw new ServletException("Failed to execute tenant-scoped filter chain", exception);
            }
        } finally {
            CorrelationContext.clear();
            TraceContextPropagator.clearTraceContext();
        }
    }

    private static void writeDomainError(HttpServletResponse response, DomainError error) throws IOException {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().write(error.message());
    }

    private static String correlationIdOrGenerate(HttpServletRequest request) {
        String value = request.getHeader(HeaderConstants.CORRELATION_HEADER);
        return (value == null || value.isBlank()) ? UUID.randomUUID().toString() : value;
    }
}
