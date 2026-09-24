package com.example.hello;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Route/contract tests for REQ-HELLO-001 (Design T-03..T-06).
 *
 * Uses the Spring web slice (WebMvcTest) so the router, GET-only restriction,
 * 405 + "Allow: GET" (ADR-007) and the 404 path are exercised over MockMvc
 * without a manually started server. The handler is the same component
 * covered at unit level by {@code HelloHandlerTest} (T-01).
 */
@WebMvcTest(HelloHandler.class)
class HelloEndpointTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getReturns200WithExactHelloWorld() throws Exception {
        mockMvc.perform(get("/api/hello"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(content().string("Hello, World!"));
    }

    @Test
    void postReturns405WithAllowGet() throws Exception {
        mockMvc.perform(post("/api/hello"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(header().string("Allow", containsString("GET")));
    }

    @Test
    void getUnknownPathReturns404() throws Exception {
        mockMvc.perform(get("/api/unknown"))
                .andExpect(status().isNotFound());
    }
}
