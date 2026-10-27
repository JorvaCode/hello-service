package com.example.hello;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StatusHandler.class)
class StatusEndpointTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getReturns200WithJsonContentType() throws Exception {
        mockMvc.perform(get("/api/status"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    void getReturnsExactStatusBody() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/status"))
                .andExpect(status().isOk())
                .andExpect(content().string("{\"status\":\"UP\"}"))
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());

        assertThat(body.size()).isEqualTo(1);
        assertThat(body.path(StatusHandler.STATUS_MEMBER).asText())
                .isEqualTo(StatusHandler.STATUS_UP);
    }

    @ParameterizedTest
    @ValueSource(strings = {"POST", "PUT", "DELETE"})
    void unsupportedMethodsReturn405WithAllowGet(String method) throws Exception {
        mockMvc.perform(requestFor(method))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(header().string("Allow", containsString("GET")));
    }

    @Test
    void patchReturns405WithAllowGet() throws Exception {
        mockMvc.perform(patch("/api/status"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(header().string("Allow", containsString("GET")));
    }

    private static MockHttpServletRequestBuilder requestFor(String method) {
        return switch (method) {
            case "POST" -> post("/api/status");
            case "PUT" -> put("/api/status");
            case "DELETE" -> delete("/api/status");
            default -> throw new IllegalArgumentException("Unsupported test method: " + method);
        };
    }
}
