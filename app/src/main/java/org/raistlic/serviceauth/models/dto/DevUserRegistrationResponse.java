package org.raistlic.serviceauth.models.dto;

import java.util.Set;

public record DevUserRegistrationResponse(
    String id,
    String username,
    Set<String> permissions
) {
}
