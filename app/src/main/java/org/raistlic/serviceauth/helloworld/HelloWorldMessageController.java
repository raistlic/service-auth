package org.raistlic.serviceauth.helloworld;

import java.net.URI;
import java.util.List;
import java.util.NoSuchElementException;
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
    ResponseEntity<HelloWorldMessage> create(@RequestBody HelloWorldMessageRequest request) {
        HelloWorldMessage created = service.create(request.message());
        return ResponseEntity
            .created(URI.create("/api/hello-world-messages/" + created.getId()))
            .body(created);
    }

    @GetMapping
    List<HelloWorldMessage> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    HelloWorldMessage findById(@PathVariable String id) {
        return service.findById(id);
    }

    @PutMapping("/{id}")
    HelloWorldMessage update(@PathVariable String id, @RequestBody HelloWorldMessageRequest request) {
        return service.update(id, request.message());
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(NoSuchElementException.class)
    ResponseEntity<Void> handleNotFound(NoSuchElementException ex) {
        return ResponseEntity.notFound().build();
    }
}
