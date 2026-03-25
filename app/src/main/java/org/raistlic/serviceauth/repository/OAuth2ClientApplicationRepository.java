package org.raistlic.serviceauth.repository;

import org.raistlic.serviceauth.models.managed.OAuth2ClientApplication;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OAuth2ClientApplicationRepository extends JpaRepository<OAuth2ClientApplication, String> {
}
