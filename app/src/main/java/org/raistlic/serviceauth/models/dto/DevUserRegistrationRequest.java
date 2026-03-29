package org.raistlic.serviceauth.models.dto;

import java.util.Set;

public record DevUserRegistrationRequest(
    String username,
    String password,
    Set<String> permissions
) {
}
