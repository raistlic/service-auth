package org.raistlic.serviceauth.helloworld;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class HelloWorldMessageTest {

    @Test
    void storesRequiredFieldsThroughConstructorAndSetter() {
        String id = UUID.randomUUID().toString();
        HelloWorldMessage message = new HelloWorldMessage(id, "hello");

        assertThat(message.getId()).isEqualTo(id);
        assertThat(message.getMessage()).isEqualTo("hello");

        message.setMessage("world");

        assertThat(message.getMessage()).isEqualTo("world");
    }

    @Test
    void rejectsNullRequiredFields() {
        assertThatThrownBy(() -> new HelloWorldMessage(null, "hello"))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("id must not be null");

        assertThatThrownBy(() -> new HelloWorldMessage(UUID.randomUUID().toString(), null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("message must not be null");
    }
}
