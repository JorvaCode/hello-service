package com.example.hello;

import java.io.IOException;
import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Request-id correlation filter (Phase 13 / ADR-011).
 *
 * For every request it binds a {@code requestId} into the SLF4J MDC under the
 * key {@code requestId} (consumed by logback-spring.xml) and echoes the same
 * value back in the {@code X-Request-ID} response header. The incoming
 * {@code X-Request-ID} header is honored when present and non-blank;
 * otherwise a random UUID is generated. The MDC entry is always removed in a
 * finally block so it never leaks across requests or threads. No new
 * dependency is introduced: only SLF4J/Logback and the Servlet API already in
 * the runtime are used (NFR-02).
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestIdFilter implements Filter {

    static final String REQUEST_ID_HEADER = "X-Request-ID";
    static final String MDC_REQUEST_ID_KEY = "requestId";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String requestId = resolveRequestId(httpRequest);
        httpResponse.setHeader(REQUEST_ID_HEADER, requestId);

        MDC.put(MDC_REQUEST_ID_KEY, requestId);
        try {
            chain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_REQUEST_ID_KEY);
        }
    }

    private static String resolveRequestId(HttpServletRequest httpRequest) {
        String headerValue = httpRequest.getHeader(REQUEST_ID_HEADER);
        if (headerValue == null || headerValue.isBlank()) {
            return UUID.randomUUID().toString();
        }
        return headerValue.trim();
    }
}