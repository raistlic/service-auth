package com.example.serviceauth.helloworld;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Objects;

@Entity
@Table(name = "hello_world_message")
public class HelloWorldMessage {

    @Id
    @Column(nullable = false, updatable = false, length = 36)
    private String id;

    @Column(nullable = false)
    private String message;

    protected HelloWorldMessage() {
    }

    public HelloWorldMessage(String id, String message) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.message = Objects.requireNonNull(message, "message must not be null");
    }

    public String getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
