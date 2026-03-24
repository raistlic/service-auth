package com.example.serviceauth.helloworld;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class HelloWorldMessageTest {

    @Test
    void storesMessageThroughConstructorAndSetter() {
        HelloWorldMessage message = new HelloWorldMessage("hello");

        assertThat(message.getId()).isNull();
        assertThat(message.getMessage()).isEqualTo("hello");

        message.setMessage("world");

        assertThat(message.getMessage()).isEqualTo("world");
    }
}
