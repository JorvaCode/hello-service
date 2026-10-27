package com.example.hello;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

class StatusHandlerTest {

    private final StatusHandler handler = new StatusHandler();

    @Test
    void returnsStatus200() {
        assertThat(handler.status().getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void returnsUpStatusBody() {
        Map<String, String> body = handler.status().getBody();

        assertThat(body).isEqualTo(Map.of(StatusHandler.STATUS_MEMBER, StatusHandler.STATUS_UP));
    }
}
