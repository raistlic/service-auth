package org.raistlic.serviceauth.helloworld;

import java.net.URI;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hello-world-messages")
class HelloWorldMessageController {

    private final HelloWorldMessageService service;

    HelloWorldMessageController(HelloWorldMessageService service) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize("hasAuthority(T(org.raistlic.serviceauth.security.PermissionNames).HELLO_WORLD_MESSAGE_WRITE)")
    ResponseEntity<HelloWorldMessage> create(@RequestBody HelloWorldMessageRequest request) {
        HelloWorldMessage created = service.create(request.message());
        return ResponseEntity
            .created(URI.create("/api/hello-world-messages/" + created.getId()))
            .body(created);
    }

    @GetMapping
    @PreAuthorize("hasAuthority(T(org.raistlic.serviceauth.security.PermissionNames).HELLO_WORLD_MESSAGE_READ)")
    List<HelloWorldMessage> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority(T(org.raistlic.serviceauth.security.PermissionNames).HELLO_WORLD_MESSAGE_READ)")
    HelloWorldMessage findById(@PathVariable String id) {
        return service.findById(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority(T(org.raistlic.serviceauth.security.PermissionNames).HELLO_WORLD_MESSAGE_WRITE)")
    HelloWorldMessage update(@PathVariable String id, @RequestBody HelloWorldMessageRequest request) {
        return service.update(id, request.message());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority(T(org.raistlic.serviceauth.security.PermissionNames).HELLO_WORLD_MESSAGE_WRITE)")
    ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(NoSuchElementException.class)
    ResponseEntity<Void> handleNotFound(NoSuchElementException ex) {
        return ResponseEntity.notFound().build();
    }
}
