package org.raistlic.serviceauth.repository;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.raistlic.serviceauth.models.managed.AppUser;
import org.raistlic.serviceauth.models.managed.Permission;
import org.raistlic.serviceauth.security.PermissionNames;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
    properties = {
        "spring.datasource.url=jdbc:h2:mem:appuserrepositorytest;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password="
    })
class AppUserRepositoryIntegrationTest {

    @Autowired
    AppUserRepository appUserRepository;

    @Autowired
    PermissionRepository permissionRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    EntityManager entityManager;

    @Test
    void findByUsernameLoadsGrantedPermissions() {
        Permission readPermission = permissionRepository.saveAndFlush(
            new Permission(UUID.randomUUID().toString(), PermissionNames.HELLO_WORLD_MESSAGE_READ));
        Permission writePermission = permissionRepository.saveAndFlush(
            new Permission(UUID.randomUUID().toString(), PermissionNames.HELLO_WORLD_MESSAGE_WRITE));

        AppUser user = new AppUser(
            UUID.randomUUID().toString(),
            "repository-user",
            passwordEncoder.encode("repository-password"));
        user.addPermission(readPermission, UUID.randomUUID().toString());
        user.addPermission(writePermission, UUID.randomUUID().toString());
        appUserRepository.saveAndFlush(user);
        entityManager.clear();

        AppUser loaded = appUserRepository.findByUsername("repository-user").orElseThrow();

        assertThat(loaded.getPasswordHash()).isNotBlank();
        assertThat(loaded.getUserPermissions())
            .extracting(grant -> grant.getPermission().getName())
            .containsExactlyInAnyOrder(
                PermissionNames.HELLO_WORLD_MESSAGE_READ,
                PermissionNames.HELLO_WORLD_MESSAGE_WRITE);
    }
}
