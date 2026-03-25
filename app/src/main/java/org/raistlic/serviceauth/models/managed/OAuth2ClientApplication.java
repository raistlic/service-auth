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
@Table(name = "oauth2_client_application")
public class OAuth2ClientApplication {

    @Id
    @Column(nullable = false, updatable = false, length = 36)
    private String clientId;

    @Column(name = "client_secret_hash", nullable = false)
    private String clientSecretHash;

    @Column(name = "client_name", nullable = false)
    private String clientName;

    @OneToMany(mappedBy = "clientApplication", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<OAuth2ClientRedirectUri> redirectUris = new LinkedHashSet<>();

    @OneToMany(mappedBy = "clientApplication", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<OAuth2ClientGrantType> grantTypes = new LinkedHashSet<>();

    @OneToMany(mappedBy = "clientApplication", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<OAuth2ClientScope> scopes = new LinkedHashSet<>();

    protected OAuth2ClientApplication() {
    }

    public OAuth2ClientApplication(String clientId, String clientSecretHash, String clientName) {
        this.clientId = Objects.requireNonNull(clientId, "clientId must not be null");
        this.clientSecretHash = Objects.requireNonNull(clientSecretHash, "clientSecretHash must not be null");
        this.clientName = Objects.requireNonNull(clientName, "clientName must not be null");
    }

    public void addRedirectUri(OAuth2ClientRedirectUri redirectUri) {
        Objects.requireNonNull(redirectUri, "redirectUri must not be null");
        redirectUri.attachTo(this);
        redirectUris.add(redirectUri);
    }

    public void addGrantType(OAuth2ClientGrantType grantType) {
        Objects.requireNonNull(grantType, "grantType must not be null");
        grantType.attachTo(this);
        grantTypes.add(grantType);
    }

    public void addScope(OAuth2ClientScope scope) {
        Objects.requireNonNull(scope, "scope must not be null");
        scope.attachTo(this);
        scopes.add(scope);
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecretHash() {
        return clientSecretHash;
    }

    public String getClientName() {
        return clientName;
    }

    public Set<OAuth2ClientRedirectUri> getRedirectUris() {
        return Set.copyOf(redirectUris);
    }

    public Set<OAuth2ClientGrantType> getGrantTypes() {
        return Set.copyOf(grantTypes);
    }

    public Set<OAuth2ClientScope> getScopes() {
        return Set.copyOf(scopes);
    }
}
