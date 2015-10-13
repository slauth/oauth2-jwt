package de.slauth.auth;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSet;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.primitives.Longs.asList;

public class User implements UserDetails {

    public static class Builder {

        private String username;
        private String password;
        private Set<GrantedAuthority> authorities = new HashSet<>();

        public Builder withUsername(String username) {
            this.username = username;
            return this;
        }

        public Builder withPassword(String password) {
            this.password = password;
            return this;
        }

        public Builder withAuthorities(Set<GrantedAuthority> authorities) {
            checkNotNull(authorities);
            this.authorities = authorities;
            return this;
        }

        public User build() {
            checkState(username != null);
            checkState(password != null);
            return new User(this);
        }
    }

    @NotNull
    private String username;
    @NotNull
    private String password;
    private Set<GrantedAuthority> authorities;

    protected User() {
    }

    private User(Builder builder) {
        this();
        this.username = builder.username;
        this.password = builder.password;
        this.authorities = ImmutableSet.copyOf(builder.authorities);
    }

    @Override
    @JsonProperty
    public String getUsername() {
        return username;
    }

    @Override
    @JsonIgnore
    public String getPassword() {
        return password;
    }

    @Override
    @JsonIgnore
    public Set<GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return true;
    }

    @JsonProperty
    protected void setUsername(String username) {
        this.username = username;
    }

    @JsonProperty
    protected void setPassword(String password) {
        this.password = password;
    }

    @JsonIgnore
    protected void setAuthorities(Set<GrantedAuthority> authorities) {
        this.authorities = authorities;
    }
}
