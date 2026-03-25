package org.raistlic.serviceauth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.serviceauth.ServiceAuthApplication;
import com.example.serviceauth.oauth2.OAuth2PersistenceConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.raistlic.serviceauth.models.dto.OAuth2ClientApplicationRegistrationRequest;
import org.raistlic.serviceauth.models.dto.OAuth2ClientApplicationRegistrationResponse;
import org.raistlic.serviceauth.service.OAuth2ClientApplicationRegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
    controllers = OAuth2ClientApplicationRegistrationController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class},
    excludeFilters = @Filter(type = FilterType.ASSIGNABLE_TYPE, classes = OAuth2PersistenceConfig.class)
)
@ContextConfiguration(classes = ServiceAuthApplication.class)
class OAuth2ClientApplicationRegistrationControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    OAuth2ClientApplicationRegistrationService service;

    @Test
    void registerReturns201WithGeneratedCredentials() throws Exception {
        when(service.register(any())).thenReturn(new OAuth2ClientApplicationRegistrationResponse(
            "client-1",
            "secret-1",
            "Sample App",
            List.of("https://client.example/callback"),
            List.of("authorization_code"),
            List.of("openid")
        ));

        mockMvc.perform(post("/api/oauth2/client-applications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new OAuth2ClientApplicationRegistrationRequest(
                    "Sample App",
                    List.of("https://client.example/callback"),
                    List.of("authorization_code"),
                    List.of("openid")
                ))))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", "/api/oauth2/client-applications/client-1"))
            .andExpect(jsonPath("$.clientId").value("client-1"))
            .andExpect(jsonPath("$.clientSecret").value("secret-1"))
            .andExpect(jsonPath("$.clientName").value("Sample App"));
    }

    @Test
    void registerReturns400ForInvalidRequest() throws Exception {
        when(service.register(any())).thenThrow(new IllegalArgumentException("invalid request"));

        mockMvc.perform(post("/api/oauth2/client-applications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new OAuth2ClientApplicationRegistrationRequest(
                    "",
                    List.of(),
                    List.of("authorization_code"),
                    List.of("openid")
                ))))
            .andExpect(status().isBadRequest());
    }
}
