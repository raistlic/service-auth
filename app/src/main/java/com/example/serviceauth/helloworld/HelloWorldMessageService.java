package com.example.serviceauth.helloworld;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HelloWorldMessageService {

    /*
     * Spring Boot auto-configures @EnableTransactionManagement when spring-boot-starter-data-jpa
     * is on the classpath. No explicit @EnableTransactionManagement annotation is needed.
     *
     * @Transactional(readOnly = true) allows the JPA provider (Hibernate) to skip dirty-checking
     * and enables the JDBC driver to use read replicas when available, improving read performance.
     */

    private final HelloWorldMessageRepository repository;

    HelloWorldMessageService(HelloWorldMessageRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public HelloWorldMessage create(String message) {
        return repository.save(new HelloWorldMessage(UUID.randomUUID().toString(), message));
    }

    @Transactional(readOnly = true)
    public List<HelloWorldMessage> findAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public HelloWorldMessage findById(String id) {
        return repository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("HelloWorldMessage not found: " + id));
    }

    @Transactional
    public HelloWorldMessage update(String id, String message) {
        HelloWorldMessage entity = repository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("HelloWorldMessage not found: " + id));
        entity.setMessage(message);
        return repository.save(entity);
    }

    @Transactional
    public void delete(String id) {
        if (!repository.existsById(id)) {
            throw new NoSuchElementException("HelloWorldMessage not found: " + id);
        }
        repository.deleteById(id);
    }
}
