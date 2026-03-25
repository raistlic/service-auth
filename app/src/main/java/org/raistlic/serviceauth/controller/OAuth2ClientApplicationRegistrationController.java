package org.raistlic.serviceauth.controller;

import java.net.URI;
import org.raistlic.serviceauth.models.dto.OAuth2ClientApplicationRegistrationRequest;
import org.raistlic.serviceauth.models.dto.OAuth2ClientApplicationRegistrationResponse;
import org.raistlic.serviceauth.service.OAuth2ClientApplicationRegistrationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/oauth2/client-applications")
class OAuth2ClientApplicationRegistrationController {

    private final OAuth2ClientApplicationRegistrationService service;

    OAuth2ClientApplicationRegistrationController(OAuth2ClientApplicationRegistrationService service) {
        this.service = service;
    }

    @PostMapping
    ResponseEntity<OAuth2ClientApplicationRegistrationResponse> register(
        @RequestBody OAuth2ClientApplicationRegistrationRequest request
    ) {
        OAuth2ClientApplicationRegistrationResponse created = service.register(request);
        return ResponseEntity
            .created(URI.create("/api/oauth2/client-applications/" + created.clientId()))
            .body(created);
    }

    @ExceptionHandler({IllegalArgumentException.class, NullPointerException.class})
    ResponseEntity<Void> handleBadRequest(RuntimeException ex) {
        return ResponseEntity.badRequest().build();
    }
}
