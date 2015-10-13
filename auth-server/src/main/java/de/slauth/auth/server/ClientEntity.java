package de.slauth.auth.server;

import javax.persistence.*;
import java.util.Date;

import static javax.persistence.GenerationType.AUTO;

@Entity
@Table(name = "clients")
public class ClientEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String secret;

    public String getId() {
        return id;
    }

    public ClientEntity setId(String id) {
        this.id = id;
        return this;
    }

    public String getSecret() {
        return secret;
    }

    public ClientEntity setSecret(String secret) {
        this.secret = secret;
        return this;
    }
}
