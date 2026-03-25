package org.raistlic.serviceauth.service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import org.raistlic.serviceauth.models.dto.OAuth2ClientApplicationRegistrationRequest;
import org.raistlic.serviceauth.models.dto.OAuth2ClientApplicationRegistrationResponse;
import org.raistlic.serviceauth.models.managed.OAuth2ClientApplication;
import org.raistlic.serviceauth.models.managed.OAuth2ClientGrantType;
import org.raistlic.serviceauth.models.managed.OAuth2ClientRedirectUri;
import org.raistlic.serviceauth.models.managed.OAuth2ClientScope;
import org.raistlic.serviceauth.repository.OAuth2ClientApplicationRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OAuth2ClientApplicationRegistrationService {

    private final OAuth2ClientApplicationRepository repository;
    private final PasswordEncoder passwordEncoder;

    OAuth2ClientApplicationRegistrationService(
        OAuth2ClientApplicationRepository repository,
        PasswordEncoder passwordEncoder
    ) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public OAuth2ClientApplicationRegistrationResponse register(OAuth2ClientApplicationRegistrationRequest request) {
        String clientName = requireText(request.clientName(), "clientName");
        List<String> redirectUris = normalizeValues(request.redirectUris(), "redirectUris");
        List<String> grantTypes = normalizeValues(request.grantTypes(), "grantTypes");
        List<String> scopes = normalizeValues(request.scopes(), "scopes");

        String clientId = UUID.randomUUID().toString();
        String clientSecret = UUID.randomUUID().toString();

        OAuth2ClientApplication application = new OAuth2ClientApplication(
            UUID.randomUUID().toString(),
            clientId,
            passwordEncoder.encode(clientSecret),
            clientName
        );

        redirectUris.forEach(uri -> application.addRedirectUri(
            new OAuth2ClientRedirectUri(UUID.randomUUID().toString(), uri)
        ));
        grantTypes.forEach(grantType -> application.addGrantType(
            new OAuth2ClientGrantType(UUID.randomUUID().toString(), grantType)
        ));
        scopes.forEach(scope -> application.addScope(
            new OAuth2ClientScope(UUID.randomUUID().toString(), scope)
        ));

        OAuth2ClientApplication saved = repository.save(application);

        return new OAuth2ClientApplicationRegistrationResponse(
            saved.getId(),
            saved.getClientId(),
            clientSecret,
            saved.getClientName(),
            redirectUris,
            grantTypes,
            scopes
        );
    }

    private static String requireText(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " must not be null");
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return trimmed;
    }

    private static List<String> normalizeValues(List<String> values, String fieldName) {
        Objects.requireNonNull(values, fieldName + " must not be null");
        if (values.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be empty");
        }

        Set<String> normalized = new LinkedHashSet<>();
        for (String value : values) {
            normalized.add(requireText(value, fieldName + " item"));
        }
        return new ArrayList<>(normalized);
    }
}
