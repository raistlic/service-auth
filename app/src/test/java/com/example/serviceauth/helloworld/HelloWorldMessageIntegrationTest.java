package com.example.serviceauth.helloworld;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.datasource.url=jdbc:h2:mem:integrationtest;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password="
    })
@AutoConfigureMockMvc
class HelloWorldMessageIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void fullCrudLifecycle() throws Exception {
        // POST → 201
        MvcResult created = mockMvc.perform(post("/api/hello-world-messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"message\":\"hello integration\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isString())
            .andExpect(jsonPath("$.message").value("hello integration"))
            .andReturn();

        String id = objectMapper.readTree(created.getResponse().getContentAsString())
            .get("id").asText();

        // GET by id → 200
        mockMvc.perform(get("/api/hello-world-messages/" + id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(id))
            .andExpect(jsonPath("$.message").value("hello integration"));

        // GET list → contains created record
        mockMvc.perform(get("/api/hello-world-messages"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[*].id", hasItem(id)));

        // PUT → 200 with updated message
        mockMvc.perform(put("/api/hello-world-messages/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"message\":\"updated\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("updated"));

        // DELETE → 204
        mockMvc.perform(delete("/api/hello-world-messages/" + id))
            .andExpect(status().isNoContent());

        // GET after delete → 404
        mockMvc.perform(get("/api/hello-world-messages/" + id))
            .andExpect(status().isNotFound());
    }

    @Test
    void missingIdReturns404() throws Exception {
        mockMvc.perform(get("/api/hello-world-messages/nonexistent-id"))
            .andExpect(status().isNotFound());
    }
}
