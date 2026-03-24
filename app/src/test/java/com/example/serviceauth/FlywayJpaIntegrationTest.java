package com.example.serviceauth;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

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
    private JdbcTemplate jdbcTemplate;

    @Test
    void appliesMigrationAndValidatesJpaSchema() {
        Integer rowCount = jdbcTemplate.queryForObject(
                "select count(*) from hello_world_message",
                Integer.class);

        assertThat(rowCount).isZero();
    }
}
