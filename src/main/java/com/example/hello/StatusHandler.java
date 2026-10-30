package com.example.hello;

import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class StatusHandler {

    static final String STATUS_MEMBER = "status";
    static final String STATUS_UP = "UP";

    @GetMapping(path = "/api/status", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Map<String, String>> status() {
        return ResponseEntity.ok(Map.of(STATUS_MEMBER, STATUS_UP));
    }
}
