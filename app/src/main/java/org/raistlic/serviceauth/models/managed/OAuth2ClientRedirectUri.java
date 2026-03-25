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
@Table(name = "oauth2_client_redirect_uri")
public class OAuth2ClientRedirectUri {

    @Id
    @Column(nullable = false, updatable = false, length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_application_id", nullable = false)
    private OAuth2ClientApplication clientApplication;

    @Column(name = "redirect_uri", nullable = false, length = 2048)
    private String redirectUri;

    protected OAuth2ClientRedirectUri() {
    }

    public OAuth2ClientRedirectUri(String id, String redirectUri) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.redirectUri = Objects.requireNonNull(redirectUri, "redirectUri must not be null");
    }

    void attachTo(OAuth2ClientApplication clientApplication) {
        this.clientApplication = Objects.requireNonNull(clientApplication, "clientApplication must not be null");
    }

    public String getId() {
        return id;
    }

    public String getRedirectUri() {
        return redirectUri;
    }
}
