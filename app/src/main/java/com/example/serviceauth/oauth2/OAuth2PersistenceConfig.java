package com.example.serviceauth.oauth2;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration(proxyBeanMethods = false)
@EntityScan(basePackages = {
    "com.example.serviceauth",
    "org.raistlic.serviceauth.models.managed"
})
@EnableJpaRepositories(basePackages = {
    "com.example.serviceauth",
    "org.raistlic.serviceauth.oauth2"
})
public class OAuth2PersistenceConfig {
}
