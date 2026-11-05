package com.example.hello;

import java.util.Optional;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import jakarta.servlet.http.HttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RequestAccessLogFilterTest {

    private final Logger logger = (Logger) LoggerFactory.getLogger(RequestAccessLogFilter.class);
    private final ListAppender<ILoggingEvent> appender = new ListAppender<>();
    private final SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();

    @BeforeEach
    void attachAppender() {
        MDC.clear();
        meterRegistry.clear();
        appender.start();
        logger.addAppender(appender);
    }

    @AfterEach
    void detachAppender() {
        logger.detachAppender(appender);
        MDC.clear();
    }

    @Test
    void logsMethodPathStatusDurationAndRequestId() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/hello");
        request.addHeader(RequestIdFilter.REQUEST_ID_HEADER, "access-log-test");
        MockHttpServletResponse response = new MockHttpServletResponse();

        new RequestIdFilter().doFilter(request, response, (idRequest, idResponse) ->
                new RequestAccessLogFilter(Optional.of(meterRegistry)).doFilter(idRequest, idResponse, (loggedRequest, loggedResponse) ->
                        ((HttpServletResponse) loggedResponse).setStatus(200)));

        assertThat(appender.list).hasSize(1);
        assertThat(appender.list.get(0).getFormattedMessage())
                .contains("method=GET")
                .contains("path=/api/hello")
                .contains("status=200")
                .contains("requestId=access-log-test")
                .containsPattern("duration_ms=\\d+");
    }

    @Test
    void logsServerErrorStatus() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/hello");
        MockHttpServletResponse response = new MockHttpServletResponse();

        new RequestAccessLogFilter(Optional.of(meterRegistry)).doFilter(request, response, (loggedRequest, loggedResponse) ->
                ((HttpServletResponse) loggedResponse).setStatus(500));

        assertThat(appender.list).hasSize(1);
        assertThat(appender.list.get(0).getFormattedMessage())
                .contains("method=POST")
                .contains("status=500")
                .contains("requestId=-");
    }

    @Test
    void omitsQueryStringFromPath() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/hello");
        request.setQueryString("token=secret");
        MockHttpServletResponse response = new MockHttpServletResponse();

        new RequestAccessLogFilter(Optional.of(meterRegistry)).doFilter(request, response, (loggedRequest, loggedResponse) ->
                ((HttpServletResponse) loggedResponse).setStatus(200));

        assertThat(appender.list.get(0).getFormattedMessage())
                .contains("path=/api/hello")
                .doesNotContain("token=secret");
    }

    @Test
    void countsEntriesWithoutRequestId() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/hello");
        MockHttpServletResponse response = new MockHttpServletResponse();

        new RequestAccessLogFilter(Optional.of(meterRegistry)).doFilter(request, response,
                (loggedRequest, loggedResponse) -> ((HttpServletResponse) loggedResponse).setStatus(200));

        assertThat(meterRegistry.get("hello_service_access_log_missing_request_id")
                .counter()
                .count())
                .isEqualTo(1.0);
    }

    @Test
    void logsStatus500AndRethrowsWhenChainFails() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/hello");
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertThatThrownBy(() ->
                new RequestAccessLogFilter(Optional.of(meterRegistry)).doFilter(request, response, (loggedRequest, loggedResponse) -> {
                    throw new IllegalStateException("boom");
                }))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("boom");

        assertThat(appender.list).hasSize(1);
        assertThat(appender.list.get(0).getFormattedMessage())
                .contains("method=GET")
                .contains("path=/api/hello")
                .contains("status=500")
                .contains("requestId=-");
    }
}
