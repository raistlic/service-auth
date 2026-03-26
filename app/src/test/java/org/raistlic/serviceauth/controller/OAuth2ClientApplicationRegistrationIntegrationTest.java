package org.raistlic.serviceauth.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.raistlic.serviceauth.ServiceAuthApplication;
import org.raistlic.serviceauth.models.dto.OAuth2ClientApplicationRegistrationRequest;
import org.raistlic.serviceauth.models.managed.OAuth2ClientApplication;
import org.raistlic.serviceauth.repository.OAuth2ClientApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(
    classes = ServiceAuthApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.datasource.url=jdbc:h2:mem:oauth2registrationtest;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password="
    })
@AutoConfigureMockMvc
class OAuth2ClientApplicationRegistrationIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    OAuth2ClientApplicationRepository repository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Test
    @Transactional
    void registerCreatesClientApplicationAndStoresHashedSecret() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/oauth2/client-applications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new OAuth2ClientApplicationRegistrationRequest(
                    "Integration App",
                    List.of("https://client.example/callback"),
                    List.of("authorization_code", "refresh_token"),
                    List.of("openid", "profile")
                ))))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", org.hamcrest.Matchers.matchesPattern("/api/oauth2/client-applications/.+")))
            .andExpect(jsonPath("$.clientId").isString())
            .andExpect(jsonPath("$.clientSecret").isString())
            .andExpect(jsonPath("$.clientName").value("Integration App"))
            .andExpect(jsonPath("$.redirectUris[0]").value("https://client.example/callback"))
            .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        String clientId = body.get("clientId").asText();
        String clientSecret = body.get("clientSecret").asText();

        OAuth2ClientApplication saved = repository.findById(clientId).orElseThrow();

        assertThat(saved.getClientName()).isEqualTo("Integration App");
        assertThat(saved.getClientSecretHash()).isNotEqualTo(clientSecret);
        assertThat(passwordEncoder.matches(clientSecret, saved.getClientSecretHash())).isTrue();
        assertThat(saved.getRedirectUris())
            .extracting(uri -> uri.getRedirectUri())
            .containsExactly("https://client.example/callback");
        assertThat(saved.getGrantTypes())
            .extracting(grantType -> grantType.getGrantType())
            .containsExactlyInAnyOrder("authorization_code", "refresh_token");
        assertThat(saved.getScopes())
            .extracting(scope -> scope.getScope())
            .containsExactlyInAnyOrder("openid", "profile");
    }

    @Test
    void registerRejectsInvalidPayload() throws Exception {
        mockMvc.perform(post("/api/oauth2/client-applications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new OAuth2ClientApplicationRegistrationRequest(
                    " ",
                    List.of(),
                    List.of("authorization_code"),
                    List.of("openid")
                ))))
            .andExpect(status().isBadRequest());
    }
}
