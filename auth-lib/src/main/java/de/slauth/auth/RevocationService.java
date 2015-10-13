package de.slauth.auth;

import org.springframework.security.oauth2.common.OAuth2AccessToken;

import java.util.Map;
import java.util.Set;

public class RevocationService {

    private final Map<String, String> revokedTokens;

    public RevocationService(Map<String, String> revokedTokens) {
        this.revokedTokens = revokedTokens;
    }

    public boolean isRevoked(OAuth2AccessToken token) {
        return revokedTokens.containsKey(token.getValue());
    }

    public Set<String> getRevokedTokens() {
        return revokedTokens.keySet();
    }

    public void revoke(String tokenValue) {
        revokedTokens.put(tokenValue, "");
    }

    public void clearRevokedTokens() {
        revokedTokens.clear();
    }
}
