package com.example.hello;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * Per-request access log (IMP-005; see
 * {@code blueprint/observability/hello-service.md}).
 *
 * Runs after {@link RequestIdFilter} so the MDC already carries the request id
 * assigned to this request. Emits one line per request with method, path,
 * status, duration and request id. The query string is deliberately excluded so
 * that client-controlled values are not copied into the log.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class RequestAccessLogFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(RequestAccessLogFilter.class);

    private final Counter missingRequestId;

    public RequestAccessLogFilter(Optional<MeterRegistry> meterRegistry) {
        this.missingRequestId = meterRegistry
                .map(registry -> Counter.builder("hello_service_access_log_missing_request_id")
                        .description("Access-log entries emitted without a request id")
                        .register(registry))
                .orElse(null);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        long startedAt = System.nanoTime();
        int status = httpResponse.getStatus();
        try {
            chain.doFilter(request, response);
            status = httpResponse.getStatus();
        } catch (IOException | ServletException | RuntimeException ex) {
            status = 500;
            throw ex;
        } finally {
            long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt);
            String requestId = MDC.get(RequestIdFilter.MDC_REQUEST_ID_KEY);
            if (requestId == null || requestId.isBlank()) {
                if (missingRequestId != null) {
                    missingRequestId.increment();
                }
                requestId = "-";
            }
            log.info(
                    "access method={} path={} status={} duration_ms={} requestId={}",
                    httpRequest.getMethod(),
                    httpRequest.getRequestURI(),
                    status,
                    durationMs,
                    requestId);
        }
    }
}
