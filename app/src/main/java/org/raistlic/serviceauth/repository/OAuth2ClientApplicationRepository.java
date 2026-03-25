package org.raistlic.serviceauth.repository;

import java.util.Optional;
import org.raistlic.serviceauth.models.managed.OAuth2ClientApplication;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OAuth2ClientApplicationRepository extends JpaRepository<OAuth2ClientApplication, String> {

    Optional<OAuth2ClientApplication> findByClientId(String clientId);
}
