package org.raistlic.serviceauth.models.managed;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.Objects;

@Entity
@Table(name = "app_user_permission")
public class AppUserPermission {

    @Id
    @Column(nullable = false, updatable = false, length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "permission_id", nullable = false, updatable = false)
    private Permission permission;

    protected AppUserPermission() {
    }

    AppUserPermission(String id, AppUser user, Permission permission) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.user = Objects.requireNonNull(user, "user must not be null");
        this.permission = Objects.requireNonNull(permission, "permission must not be null");
    }

    public String getId() {
        return id;
    }

    public AppUser getUser() {
        return user;
    }

    public Permission getPermission() {
        return permission;
    }
}
