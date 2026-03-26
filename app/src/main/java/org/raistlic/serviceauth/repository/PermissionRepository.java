package org.raistlic.serviceauth.repository;

import java.util.Optional;
import org.raistlic.serviceauth.models.managed.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<Permission, String> {
    Optional<Permission> findByName(String name);
}
