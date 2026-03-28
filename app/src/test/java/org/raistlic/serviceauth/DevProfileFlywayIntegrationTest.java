package org.raistlic.serviceauth;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.raistlic.serviceauth.models.managed.AppUser;
import org.raistlic.serviceauth.repository.AppUserRepository;
import org.raistlic.serviceauth.security.PermissionNames;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
                "spring.datasource.url=jdbc:h2:mem:devprofiletest;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
                "spring.datasource.driver-class-name=org.h2.Driver",
                "spring.datasource.username=sa",
                "spring.datasource.password="
        })
@ActiveProfiles("dev")
class DevProfileFlywayIntegrationTest {

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void devProfileLoadsSeededBasicAuthUsersFromDevFlywayPath() {
        AppUser devAdmin = appUserRepository.findByUsername("dev-admin").orElseThrow();
        AppUser devReader = appUserRepository.findByUsername("dev-reader").orElseThrow();

        assertThat(passwordEncoder.matches("dev-admin-password", devAdmin.getPasswordHash())).isTrue();
        assertThat(passwordEncoder.matches("dev-reader-password", devReader.getPasswordHash())).isTrue();

        assertThat(devAdmin.getUserPermissions())
                .extracting(grant -> grant.getPermission().getName())
                .containsExactlyInAnyOrder(
                        PermissionNames.HELLO_WORLD_MESSAGE_READ,
                        PermissionNames.HELLO_WORLD_MESSAGE_WRITE);

        assertThat(devReader.getUserPermissions())
                .extracting(grant -> grant.getPermission().getName())
                .containsExactly(PermissionNames.HELLO_WORLD_MESSAGE_READ);
    }
}
