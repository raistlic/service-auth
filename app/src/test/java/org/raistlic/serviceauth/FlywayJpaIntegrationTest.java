package org.raistlic.serviceauth;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.raistlic.serviceauth.helloworld.HelloWorldMessage;
import org.raistlic.serviceauth.helloworld.HelloWorldMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
                "spring.datasource.url=jdbc:h2:mem:flywaytest;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
                "spring.datasource.driver-class-name=org.h2.Driver",
                "spring.datasource.username=sa",
                "spring.datasource.password="
        })
class FlywayJpaIntegrationTest {

    @Autowired
    private HelloWorldMessageRepository helloWorldMessageRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @Transactional
    void appliesMigrationAndPersistsEntityThroughJpaRepository() {
        assertThat(helloWorldMessageRepository.count()).isZero();

        String id = UUID.randomUUID().toString();
        HelloWorldMessage saved = helloWorldMessageRepository.saveAndFlush(
                new HelloWorldMessage(id, "hello world"));
        entityManager.clear();

        assertThat(saved.getId()).isEqualTo(id);
        assertThat(helloWorldMessageRepository.findById(saved.getId()))
                .map(HelloWorldMessage::getMessage)
                .contains("hello world");
    }
}
