package org.raistlic.serviceauth.repository;

import java.util.Optional;
import org.raistlic.serviceauth.models.managed.AppUser;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserRepository extends JpaRepository<AppUser, String> {

    @EntityGraph(attributePaths = {"userPermissions", "userPermissions.permission"})
    Optional<AppUser> findByUsername(String username);
}
