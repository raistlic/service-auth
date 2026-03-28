package org.raistlic.serviceauth.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.raistlic.serviceauth.models.dto.DevUserRegistrationRequest;
import org.raistlic.serviceauth.models.managed.AppUser;
import org.raistlic.serviceauth.repository.AppUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.datasource.url=jdbc:h2:mem:devuserregistrationtest;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password="
    })
@ActiveProfiles("dev")
@AutoConfigureMockMvc
class DevUserRegistrationIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    AppUserRepository appUserRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Test
    @Transactional
    void registerCreatesUserWithEncodedPasswordAndPermissions() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/dev/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new DevUserRegistrationRequest(
                    "dev-created-user",
                    "plain-password",
                    Set.of("custom:one", "custom:two")
                ))))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", org.hamcrest.Matchers.matchesPattern("/api/dev/users/.+")))
            .andExpect(jsonPath("$.id").isString())
            .andExpect(jsonPath("$.username").value("dev-created-user"))
            .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        AppUser saved = appUserRepository.findById(body.get("id").asText()).orElseThrow();

        assertThat(saved.getUsername()).isEqualTo("dev-created-user");
        assertThat(passwordEncoder.matches("plain-password", saved.getPasswordHash())).isTrue();
        assertThat(saved.getUserPermissions())
            .extracting(grant -> grant.getPermission().getName())
            .containsExactlyInAnyOrder("custom:one", "custom:two");
    }

    @Test
    void registerRejectsInvalidPayload() throws Exception {
        mockMvc.perform(post("/api/dev/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new DevUserRegistrationRequest(
                    " ",
                    "",
                    Set.of()
                ))))
            .andExpect(status().isBadRequest());
    }
}
