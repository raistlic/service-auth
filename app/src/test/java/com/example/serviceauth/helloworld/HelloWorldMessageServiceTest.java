package com.example.serviceauth.helloworld;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HelloWorldMessageServiceTest {

    @Mock
    HelloWorldMessageRepository repository;

    @InjectMocks
    HelloWorldMessageService service;

    @Test
    void createPersistsAndReturnsMessage() {
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        HelloWorldMessage result = service.create("hello");

        assertThat(result.getId()).isNotNull();
        assertThat(result.getMessage()).isEqualTo("hello");
        verify(repository).save(any(HelloWorldMessage.class));
    }

    @Test
    void findAllReturnsAllMessages() {
        String id = UUID.randomUUID().toString();
        when(repository.findAll()).thenReturn(List.of(new HelloWorldMessage(id, "hello")));

        List<HelloWorldMessage> result = service.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMessage()).isEqualTo("hello");
    }

    @Test
    void findByIdReturnsMessageWhenFound() {
        String id = UUID.randomUUID().toString();
        when(repository.findById(id)).thenReturn(Optional.of(new HelloWorldMessage(id, "hello")));

        HelloWorldMessage result = service.findById(id);

        assertThat(result.getId()).isEqualTo(id);
    }

    @Test
    void findByIdThrowsWhenNotFound() {
        when(repository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById("missing"))
            .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void updateChangesMessageAndReturns() {
        String id = UUID.randomUUID().toString();
        HelloWorldMessage existing = new HelloWorldMessage(id, "old");
        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        HelloWorldMessage result = service.update(id, "new");

        assertThat(result.getMessage()).isEqualTo("new");
        verify(repository).save(existing);
    }

    @Test
    void updateThrowsWhenNotFound() {
        when(repository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update("missing", "new"))
            .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void deleteRemovesEntity() {
        String id = UUID.randomUUID().toString();
        when(repository.existsById(id)).thenReturn(true);

        service.delete(id);

        verify(repository).deleteById(id);
    }

    @Test
    void deleteThrowsWhenNotFound() {
        when(repository.existsById("missing")).thenReturn(false);

        assertThatThrownBy(() -> service.delete("missing"))
            .isInstanceOf(NoSuchElementException.class);
    }
}
