package org.raistlic.serviceauth.service;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import org.raistlic.serviceauth.models.dto.DevUserRegistrationRequest;
import org.raistlic.serviceauth.models.dto.DevUserRegistrationResponse;
import org.raistlic.serviceauth.models.managed.AppUser;
import org.raistlic.serviceauth.models.managed.Permission;
import org.raistlic.serviceauth.repository.AppUserRepository;
import org.raistlic.serviceauth.repository.PermissionRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Profile("dev")
public class DevUserRegistrationService {

    private final AppUserRepository appUserRepository;
    private final PermissionRepository permissionRepository;
    private final PasswordEncoder passwordEncoder;

    public DevUserRegistrationService(
        AppUserRepository appUserRepository,
        PermissionRepository permissionRepository,
        PasswordEncoder passwordEncoder
    ) {
        this.appUserRepository = appUserRepository;
        this.permissionRepository = permissionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public DevUserRegistrationResponse register(DevUserRegistrationRequest request) {
        String username = requireText(request.username(), "username");
        String password = requireText(request.password(), "password");
        Set<String> permissions = normalizePermissions(request.permissions());

        AppUser user = new AppUser(UUID.randomUUID().toString(), username, passwordEncoder.encode(password));
        for (String permissionName : permissions) {
            Permission permission = permissionRepository.findByName(permissionName)
                .orElseGet(() -> permissionRepository.save(new Permission(UUID.randomUUID().toString(), permissionName)));
            user.addPermission(permission, UUID.randomUUID().toString());
        }

        try {
            AppUser saved = appUserRepository.saveAndFlush(user);
            return new DevUserRegistrationResponse(saved.getId(), saved.getUsername(), permissions);
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalArgumentException("username already exists", ex);
        }
    }

    private static String requireText(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " must not be null");
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return trimmed;
    }

    private static Set<String> normalizePermissions(Set<String> permissions) {
        Objects.requireNonNull(permissions, "permissions must not be null");
        if (permissions.isEmpty()) {
            throw new IllegalArgumentException("permissions must not be empty");
        }

        Set<String> normalized = new LinkedHashSet<>();
        for (String permission : permissions) {
            normalized.add(requireText(permission, "permissions item"));
        }
        return Set.copyOf(normalized);
    }
}
