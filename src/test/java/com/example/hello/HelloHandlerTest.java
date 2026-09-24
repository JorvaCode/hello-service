package com.example.hello;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link HelloHandler} (Design T-01).
 *
 * Verifies the component's observable contract on the success path without
 * a running server: status 200, plain-text body containing "Hello", exact
 * "Hello, World!" value and UTF-8 charset.
 *
 * Maps to: FR-03, FR-04, NFR-01, ADR-005, ADR-006, AC-1, AC-2.
 */
class HelloHandlerTest {

    private final HelloHandler handler = new HelloHandler();

    @Test
    void returnsStatus200() {
        assertThat(handler.hello().getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void returnsExactHelloWorldBody() {
        assertThat(handler.hello().getBody()).isEqualTo("Hello, World!");
    }

    @Test
    void returnsPlainTextContentTypeWithUtf8() {
        MediaType contentType = handler.hello().getHeaders().getContentType();

        assertThat(contentType).isNotNull();
        assertThat(contentType.getType()).isEqualTo("text");
        assertThat(contentType.getSubtype()).isEqualTo("plain");
        assertThat(contentType.getCharset()).isEqualTo(StandardCharsets.UTF_8);
    }
}
