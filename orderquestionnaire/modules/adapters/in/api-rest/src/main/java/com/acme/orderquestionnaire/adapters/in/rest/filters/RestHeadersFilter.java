package com.acme.adapters.in.rest.filters;

//import com.acme.shared.constants.HeaderConstants;
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpFilter;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.springframework.stereotype.Component;
//
//import java.io.IOException;
//import java.util.UUID;
//
//@Component
//public class RestHeadersFilter extends HttpFilter {
//
//    @Override
//    protected void doFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
//            throws IOException, ServletException {
//        var corr = headerOrGenerate(req, HeaderConstants.CORRELATION_HEADER);
//        res.setHeader(HeaderConstants.CORRELATION_HEADER, corr);
//
//        if (req.getRequestURI().startsWith("/api/animals")) {
//            requireHeader(req, HeaderConstants.JOURNEY_HEADER);
//            requireHeader(req, HeaderConstants.CHANNEL_HEADER);
//            res.setHeader(HeaderConstants.JOURNEY_HEADER, req.getHeader(HeaderConstants.JOURNEY_HEADER));
//            res.setHeader(HeaderConstants.CHANNEL_HEADER, req.getHeader(HeaderConstants.CHANNEL_HEADER));
//        }
//        chain.doFilter(req, res);
//    }
//
//    private static void requireHeader(HttpServletRequest req, String name) {
//        if (req.getHeader(name) == null || req.getHeader(name).isBlank())
//            throw new RuntimeException("Missing required header: " + name);
//    }
//
//    private static String headerOrGenerate(HttpServletRequest req, String name) {
//        var v = req.getHeader(name);
//        return (v == null || v.isBlank()) ? UUID.randomUUID().toString() : v;
//    }
//}
