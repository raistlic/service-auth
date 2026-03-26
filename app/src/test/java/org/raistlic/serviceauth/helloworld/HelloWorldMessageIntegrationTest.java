package org.raistlic.serviceauth.helloworld;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.raistlic.serviceauth.models.managed.AppUser;
import org.raistlic.serviceauth.models.managed.Permission;
import org.raistlic.serviceauth.repository.AppUserRepository;
import org.raistlic.serviceauth.repository.PermissionRepository;
import org.raistlic.serviceauth.security.PermissionNames;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
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

    private static final String ADMIN_USERNAME = "hello-admin";
    private static final String ADMIN_PASSWORD = "hello-admin-password";
    private static final String READER_USERNAME = "hello-reader";
    private static final String READER_PASSWORD = "hello-reader-password";

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    AppUserRepository appUserRepository;

    @Autowired
    PermissionRepository permissionRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Test
    void fullCrudLifecycle() throws Exception {
        ensureUserWithPermissions(ADMIN_USERNAME, ADMIN_PASSWORD,
            PermissionNames.HELLO_WORLD_MESSAGE_READ,
            PermissionNames.HELLO_WORLD_MESSAGE_WRITE);

        // POST → 201
        MvcResult created = mockMvc.perform(post("/api/hello-world-messages")
                .header("Authorization", basicAuth(ADMIN_USERNAME, ADMIN_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"message\":\"hello integration\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isString())
            .andExpect(jsonPath("$.message").value("hello integration"))
            .andReturn();

        String id = objectMapper.readTree(created.getResponse().getContentAsString())
            .get("id").asText();

        // GET by id → 200
        mockMvc.perform(get("/api/hello-world-messages/" + id)
                .header("Authorization", basicAuth(ADMIN_USERNAME, ADMIN_PASSWORD)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(id))
            .andExpect(jsonPath("$.message").value("hello integration"));

        // GET list → contains created record
        mockMvc.perform(get("/api/hello-world-messages")
                .header("Authorization", basicAuth(ADMIN_USERNAME, ADMIN_PASSWORD)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[*].id", hasItem(id)));

        // PUT → 200 with updated message
        mockMvc.perform(put("/api/hello-world-messages/" + id)
                .header("Authorization", basicAuth(ADMIN_USERNAME, ADMIN_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"message\":\"updated\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("updated"));

        // DELETE → 204
        mockMvc.perform(delete("/api/hello-world-messages/" + id)
                .header("Authorization", basicAuth(ADMIN_USERNAME, ADMIN_PASSWORD)))
            .andExpect(status().isNoContent());

        // GET after delete → 404
        mockMvc.perform(get("/api/hello-world-messages/" + id)
                .header("Authorization", basicAuth(ADMIN_USERNAME, ADMIN_PASSWORD)))
            .andExpect(status().isNotFound());
    }

    @Test
    void missingIdReturns404() throws Exception {
        ensureUserWithPermissions(ADMIN_USERNAME, ADMIN_PASSWORD, PermissionNames.HELLO_WORLD_MESSAGE_READ);

        mockMvc.perform(get("/api/hello-world-messages/nonexistent-id")
                .header("Authorization", basicAuth(ADMIN_USERNAME, ADMIN_PASSWORD)))
            .andExpect(status().isNotFound());
    }

    @Test
    void missingCredentialsReturn401() throws Exception {
        mockMvc.perform(get("/api/hello-world-messages"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void invalidCredentialsReturn401() throws Exception {
        ensureUserWithPermissions(ADMIN_USERNAME, ADMIN_PASSWORD, PermissionNames.HELLO_WORLD_MESSAGE_READ);

        mockMvc.perform(get("/api/hello-world-messages")
                .header("Authorization", basicAuth(ADMIN_USERNAME, "wrong-password")))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void userWithoutWritePermissionGets403() throws Exception {
        ensureUserWithPermissions(READER_USERNAME, READER_PASSWORD, PermissionNames.HELLO_WORLD_MESSAGE_READ);

        mockMvc.perform(post("/api/hello-world-messages")
                .header("Authorization", basicAuth(READER_USERNAME, READER_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"message\":\"denied\"}"))
            .andExpect(status().isForbidden());
    }

    private void ensureUserWithPermissions(String username, String password, String... permissionNames) {
        AppUser user = appUserRepository.findByUsername(username)
            .orElseGet(() -> new AppUser(UUID.randomUUID().toString(), username, passwordEncoder.encode(password)));
        Set<String> existingPermissions = new HashSet<>();
        user.getUserPermissions().forEach(grant -> existingPermissions.add(grant.getPermission().getName()));

        for (String permissionName : permissionNames) {
            if (existingPermissions.contains(permissionName)) {
                continue;
            }
            Permission permission = permissionRepository.findByName(permissionName)
                .orElseGet(() -> permissionRepository.saveAndFlush(new Permission(UUID.randomUUID().toString(), permissionName)));
            user.addPermission(permission, UUID.randomUUID().toString());
        }
        appUserRepository.saveAndFlush(user);
    }

    private static String basicAuth(String username, String password) {
        String token = Base64.getEncoder()
            .encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
        return "Basic " + token;
    }
}
