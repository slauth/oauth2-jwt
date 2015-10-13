package de.slauth.auth.server;

import javax.persistence.*;
import java.util.Date;

import static javax.persistence.GenerationType.AUTO;

@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(name = "scopes")
    private String scopes;

    public String getUsername() {
        return username;
    }

    public UserEntity setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public UserEntity setPassword(String password) {
        this.password = password;
        return this;
    }

    public String getScopes() {
        return scopes;
    }

    public UserEntity setScopes(String scopes) {
        this.scopes = scopes;
        return this;
    }
}
