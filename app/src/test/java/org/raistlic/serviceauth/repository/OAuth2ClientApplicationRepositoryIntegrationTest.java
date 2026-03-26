package org.raistlic.serviceauth.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.persistence.EntityManager;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.raistlic.serviceauth.ServiceAuthApplication;
import org.raistlic.serviceauth.models.managed.OAuth2ClientApplication;
import org.raistlic.serviceauth.models.managed.OAuth2ClientGrantType;
import org.raistlic.serviceauth.models.managed.OAuth2ClientRedirectUri;
import org.raistlic.serviceauth.models.managed.OAuth2ClientScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(
    classes = ServiceAuthApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
    properties = {
        "spring.datasource.url=jdbc:h2:mem:oauth2clienttest;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password="
    })
class OAuth2ClientApplicationRepositoryIntegrationTest {

    @Autowired
    OAuth2ClientApplicationRepository repository;

    @Autowired
    EntityManager entityManager;

    @Test
    @Transactional
    void persistsAndLoadsRegisteredClientApplication() {
        OAuth2ClientApplication application = new OAuth2ClientApplication(
            "sample-client",
            "{bcrypt}$2a$10$abcdefghijklmnopqrstuv",
            "Sample Client"
        );
        application.addRedirectUri(new OAuth2ClientRedirectUri(UUID.randomUUID().toString(), "https://client.example/callback"));
        application.addGrantType(new OAuth2ClientGrantType(UUID.randomUUID().toString(), "authorization_code"));
        application.addGrantType(new OAuth2ClientGrantType(UUID.randomUUID().toString(), "refresh_token"));
        application.addScope(new OAuth2ClientScope(UUID.randomUUID().toString(), "openid"));
        application.addScope(new OAuth2ClientScope(UUID.randomUUID().toString(), "profile"));

        repository.saveAndFlush(application);
        entityManager.clear();

        OAuth2ClientApplication loaded = repository.findById("sample-client").orElseThrow();

        assertThat(loaded.getClientName()).isEqualTo("Sample Client");
        assertThat(loaded.getClientSecretHash()).startsWith("{bcrypt}");
        assertThat(loaded.getRedirectUris())
            .extracting(OAuth2ClientRedirectUri::getRedirectUri)
            .containsExactly("https://client.example/callback");
        assertThat(loaded.getGrantTypes())
            .extracting(OAuth2ClientGrantType::getGrantType)
            .containsExactlyInAnyOrder("authorization_code", "refresh_token");
        assertThat(loaded.getScopes())
            .extracting(OAuth2ClientScope::getScope)
            .containsExactlyInAnyOrder("openid", "profile");
    }

    @Test
    @Transactional
    void rejectsDuplicateClientId() {
        repository.saveAndFlush(new OAuth2ClientApplication(
            "duplicate-client",
            "{bcrypt}$2a$10$abcdefghijklmnopqrstuv",
            "First Client"
        ));
        entityManager.clear();

        assertThatThrownBy(() -> {
            entityManager.persist(new OAuth2ClientApplication(
                "duplicate-client",
                "{bcrypt}$2a$10$zzzzzzzzzzzzzzzzzzzzzz",
                "Second Client"
            ));
            entityManager.flush();
        }).isInstanceOfAny(RuntimeException.class);
    }

    @Test
    @Transactional
    void rejectsMissingRequiredClientName() {
        assertThatThrownBy(() -> repository.saveAndFlush(new OAuth2ClientApplication(
            "missing-name-client",
            "{bcrypt}$2a$10$abcdefghijklmnopqrstuv",
            null
        )))
            .isInstanceOf(NullPointerException.class);
    }
}
