package com.example.serviceauth.helloworld;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.http.MediaType;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.example.serviceauth.oauth2.OAuth2PersistenceConfig;

@WebMvcTest(
    controllers = HelloWorldMessageController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class},
    excludeFilters = @Filter(type = FilterType.ASSIGNABLE_TYPE, classes = OAuth2PersistenceConfig.class)
)
class HelloWorldMessageControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    HelloWorldMessageService service;

    @Test
    void createReturns201WithBody() throws Exception {
        String id = UUID.randomUUID().toString();
        when(service.create("hello")).thenReturn(new HelloWorldMessage(id, "hello"));

        mockMvc.perform(post("/api/hello-world-messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new HelloWorldMessageRequest("hello"))))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", "/api/hello-world-messages/" + id))
            .andExpect(jsonPath("$.id").value(id))
            .andExpect(jsonPath("$.message").value("hello"));
    }

    @Test
    void findAllReturns200WithList() throws Exception {
        String id = UUID.randomUUID().toString();
        when(service.findAll()).thenReturn(List.of(new HelloWorldMessage(id, "hello")));

        mockMvc.perform(get("/api/hello-world-messages"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(id))
            .andExpect(jsonPath("$[0].message").value("hello"));
    }

    @Test
    void findByIdReturns200WhenFound() throws Exception {
        String id = UUID.randomUUID().toString();
        when(service.findById(id)).thenReturn(new HelloWorldMessage(id, "hello"));

        mockMvc.perform(get("/api/hello-world-messages/" + id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(id))
            .andExpect(jsonPath("$.message").value("hello"));
    }

    @Test
    void findByIdReturns404WhenNotFound() throws Exception {
        when(service.findById("missing")).thenThrow(new NoSuchElementException("not found"));

        mockMvc.perform(get("/api/hello-world-messages/missing"))
            .andExpect(status().isNotFound());
    }

    @Test
    void updateReturns200WhenFound() throws Exception {
        String id = UUID.randomUUID().toString();
        when(service.update(eq(id), eq("updated"))).thenReturn(new HelloWorldMessage(id, "updated"));

        mockMvc.perform(put("/api/hello-world-messages/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new HelloWorldMessageRequest("updated"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("updated"));
    }

    @Test
    void updateReturns404WhenNotFound() throws Exception {
        when(service.update(eq("missing"), any())).thenThrow(new NoSuchElementException("not found"));

        mockMvc.perform(put("/api/hello-world-messages/missing")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new HelloWorldMessageRequest("x"))))
            .andExpect(status().isNotFound());
    }

    @Test
    void deleteReturns204() throws Exception {
        String id = UUID.randomUUID().toString();

        mockMvc.perform(delete("/api/hello-world-messages/" + id))
            .andExpect(status().isNoContent());

        verify(service).delete(id);
    }

    @Test
    void deleteReturns404WhenNotFound() throws Exception {
        doThrow(new NoSuchElementException("not found")).when(service).delete("missing");

        mockMvc.perform(delete("/api/hello-world-messages/missing"))
            .andExpect(status().isNotFound());
    }
}
