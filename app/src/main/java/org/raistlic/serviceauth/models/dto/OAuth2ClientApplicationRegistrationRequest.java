package org.raistlic.serviceauth.models.dto;

import java.util.List;

public record OAuth2ClientApplicationRegistrationRequest(
    String clientName,
    List<String> redirectUris,
    List<String> grantTypes,
    List<String> scopes
) {
}
