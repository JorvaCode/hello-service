package com.example.hello;

import java.nio.charset.StandardCharsets;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Handles GET /api/hello (REQ-HELLO-001).
 *
 * Route registration is done by Spring's mapping at startup (ADR-008); the
 * GET-only restriction and the automatic 405 + "Allow: GET" for other methods
 * are enforced at the routing layer (ADR-007).
 */
@RestController
class HelloHandler {

    static final String MESSAGE = "Hello, World!";

    @GetMapping(path = "/api/hello", produces = MediaType.TEXT_PLAIN_VALUE)
    ResponseEntity<String> hello() {
        return ResponseEntity.ok()
                .contentType(new MediaType("text", "plain", StandardCharsets.UTF_8))
                .body(MESSAGE);
    }
}