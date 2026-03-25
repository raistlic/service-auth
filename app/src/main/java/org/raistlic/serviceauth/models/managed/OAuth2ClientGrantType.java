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
@Table(name = "oauth2_client_grant_type")
public class OAuth2ClientGrantType {

    @Id
    @Column(nullable = false, updatable = false, length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_application_id", nullable = false)
    private OAuth2ClientApplication clientApplication;

    @Column(name = "grant_type", nullable = false, length = 100)
    private String grantType;

    protected OAuth2ClientGrantType() {
    }

    public OAuth2ClientGrantType(String id, String grantType) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.grantType = Objects.requireNonNull(grantType, "grantType must not be null");
    }

    void attachTo(OAuth2ClientApplication clientApplication) {
        this.clientApplication = Objects.requireNonNull(clientApplication, "clientApplication must not be null");
    }

    public String getId() {
        return id;
    }

    public String getGrantType() {
        return grantType;
    }
}
