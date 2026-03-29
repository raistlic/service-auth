package org.raistlic.serviceauth.controller;

import java.net.URI;
import org.raistlic.serviceauth.models.dto.DevUserRegistrationRequest;
import org.raistlic.serviceauth.models.dto.DevUserRegistrationResponse;
import org.raistlic.serviceauth.service.DevUserRegistrationService;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Profile("dev")
@RestController
@RequestMapping("/api/dev/users")
class DevUserRegistrationController {

    private final DevUserRegistrationService service;

    DevUserRegistrationController(DevUserRegistrationService service) {
        this.service = service;
    }

    @PostMapping
    ResponseEntity<DevUserRegistrationResponse> register(@RequestBody DevUserRegistrationRequest request) {
        DevUserRegistrationResponse created = service.register(request);
        return ResponseEntity
            .created(URI.create("/api/dev/users/" + created.id()))
            .body(created);
    }

    @ExceptionHandler({IllegalArgumentException.class, NullPointerException.class})
    ResponseEntity<Void> handleBadRequest(RuntimeException ex) {
        return ResponseEntity.badRequest().build();
    }
}
