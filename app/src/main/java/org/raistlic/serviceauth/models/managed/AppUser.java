package org.raistlic.serviceauth.models.managed;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "app_user")
public class AppUser {

    @Id
    @Column(nullable = false, updatable = false, length = 36)
    private String id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<AppUserPermission> userPermissions = new LinkedHashSet<>();

    protected AppUser() {
    }

    public AppUser(String id, String username, String passwordHash) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.username = Objects.requireNonNull(username, "username must not be null");
        this.passwordHash = Objects.requireNonNull(passwordHash, "passwordHash must not be null");
    }

    public void addPermission(Permission permission, String userPermissionId) {
        Objects.requireNonNull(permission, "permission must not be null");
        userPermissions.add(new AppUserPermission(userPermissionId, this, permission));
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Set<AppUserPermission> getUserPermissions() {
        return Set.copyOf(userPermissions);
    }
}
