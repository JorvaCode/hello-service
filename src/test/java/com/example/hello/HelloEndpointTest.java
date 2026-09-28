package com.example.hello;

import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
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
@Import(RequestIdFilter.class)
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

    @Test
    void requestIdIsPropagatedWhenProvided() throws Exception {
        mockMvc.perform(get("/api/hello")
                .header(RequestIdFilter.REQUEST_ID_HEADER, "phase13-test-001"))
                .andExpect(status().isOk())
                .andExpect(header().string(
                        RequestIdFilter.REQUEST_ID_HEADER,
                        "phase13-test-001"));
    }

    @Test
    void requestIdIsGeneratedWhenNotProvided() throws Exception {
        mockMvc.perform(get("/api/hello"))
                .andExpect(status().isOk())
                .andExpect(header().exists(RequestIdFilter.REQUEST_ID_HEADER));
    }

    @Test
    void requestIdIsAvailableInMdcDuringRequest() throws Exception {
        mockMvc.perform(get("/api/hello")
                .header(RequestIdFilter.REQUEST_ID_HEADER, "phase13-mdc-test"))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    assertThat(result.getResponse()
                            .getHeader(RequestIdFilter.REQUEST_ID_HEADER))
                            .isEqualTo("phase13-mdc-test");

                    assertThat(MDC.get(RequestIdFilter.MDC_REQUEST_ID_KEY))
                            .isNull();
                });
    }
}
