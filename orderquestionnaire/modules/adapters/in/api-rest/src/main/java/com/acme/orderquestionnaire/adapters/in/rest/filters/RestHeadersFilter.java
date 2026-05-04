package com.acme.orderquestionnaire.adapters.in.rest.filters;

import com.acme.shared.constants.HeaderConstants;
import com.acme.shared.observability.CorrelationContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

/**
 * Servlet filter responsible for request-scoped correlation context.
 * <p>
 * For every inbound HTTP request the filter:
 * <ol>
 *   <li>Reads (or generates) {@code X-Correlation-Id} and echoes it in the response.</li>
 *   <li>Reads {@code X-Flow-Id} (if present) and echoes it in the response.</li>
 *   <li>Populates {@link CorrelationContext} (MDC) so that all log statements
 *       within the request include the correlation/flow identifiers.</li>
 *   <li>Clears the MDC in a {@code finally} block to prevent context leakage
 *       in thread-pool environments.</li>
 * </ol>
 */
@Component
public class RestHeadersFilter extends HttpFilter {

    @Override
    protected void doFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        String correlationId = headerOrGenerate(req, HeaderConstants.CORRELATION_HEADER);
        String flowId = req.getHeader(HeaderConstants.FLOW_HEADER);

        res.setHeader(HeaderConstants.CORRELATION_HEADER, correlationId);
        if (flowId != null && !flowId.isBlank()) {
            res.setHeader(HeaderConstants.FLOW_HEADER, flowId);
        }

        try {
            CorrelationContext.populate(correlationId, flowId);
            chain.doFilter(req, res);
        } finally {
            CorrelationContext.clear();
        }
    }

    private static String headerOrGenerate(HttpServletRequest req, String headerName) {
        var v = req.getHeader(headerName);
        return (v == null || v.isBlank()) ? UUID.randomUUID().toString() : v;
    }
}
