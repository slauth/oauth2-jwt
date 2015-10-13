package de.slauth.auth;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.provider.ClientDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkState;

public class Client implements ClientDetails {

    public static class Builder {

        private String id;
        private String secret;
        private Set<String> resourceIds = new HashSet<>();
        private Set<String> scope = Sets.newHashSet("default");
        private Integer accessTokenValiditySeconds;
        private Integer refreshTokenValiditySeconds;

        public Builder withId(String id) {
            this.id = id;
            return this;
        }

        public Builder withSecret(String secret) {
            this.secret = secret;
            return this;
        }

        public Builder withResourceIds(Set<String> resourceIds) {
            this.resourceIds = resourceIds;
            return this;
        }

        public Builder withScope(Set<String> scope) {
            this.scope = scope;
            return this;
        }

        public Builder withAccessTokenValiditySeconds(Integer accessTokenValiditySeconds) {
            this.accessTokenValiditySeconds = accessTokenValiditySeconds;
            return this;
        }

        public Builder withRefreshTokenValiditySeconds(Integer refreshTokenValiditySeconds) {
            this.refreshTokenValiditySeconds = refreshTokenValiditySeconds;
            return this;
        }

        public Client build() {
            checkState(id != null);
            checkState(secret != null);
            return new Client(this);
        }
    }

    private String id;
    private String secret;
    private Set<String> resourceIds;
    private Set<String> scope;
    private Integer accessTokenValiditySeconds;
    private Integer refreshTokenValiditySeconds;

    protected Client() {
    }

    private Client(Builder builder) {
        this();
        this.id = builder.id;
        this.secret = builder.secret;
        this.resourceIds = ImmutableSet.copyOf(builder.resourceIds);
        this.scope = ImmutableSet.copyOf(builder.scope);
        this.accessTokenValiditySeconds = builder.accessTokenValiditySeconds;
        this.refreshTokenValiditySeconds = builder.refreshTokenValiditySeconds;
    }

    @Override
    public String getClientId() {
        return id;
    }

    @Override
    public Set<String> getResourceIds() {
        return resourceIds;
    }

    @Override
    public boolean isSecretRequired() {
        return true;
    }

    @Override
    public String getClientSecret() {
        return secret;
    }

    @Override
    public boolean isScoped() {
        return false;
    }

    @Override
    public Set<String> getScope() {
        return scope;
    }

    @Override
    public Set<String> getAuthorizedGrantTypes() {
        return ImmutableSet.of("password", "refresh_token");
    }

    @Override
    public Set<String> getRegisteredRedirectUri() {
        return null;
    }

    @Override
    public Collection<GrantedAuthority> getAuthorities() {
        return ImmutableSet.of();
    }

    @Override
    public Integer getAccessTokenValiditySeconds() {
        return accessTokenValiditySeconds;
    }

    @Override
    public Integer getRefreshTokenValiditySeconds() {
        return refreshTokenValiditySeconds;
    }

    @Override
    public boolean isAutoApprove(String scope) {
        return false;
    }

    @Override
    public Map<String, Object> getAdditionalInformation() {
        return null;
    }
}
