package de.slauth.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class CustomJwtTokenStore extends JwtTokenStore {

    private static final Date EXPIRED_DATE = new Date(0L);

    @Autowired RevocationService revocationService;

    public CustomJwtTokenStore(JwtAccessTokenConverter jwtTokenEnhancer) {
        super(jwtTokenEnhancer);
    }

    @Override
    public OAuth2AccessToken readAccessToken(String tokenValue) {
        OAuth2AccessToken token = super.readAccessToken(tokenValue);
        if (revocationService.isRevoked(token)) {
            DefaultOAuth2AccessToken revokedToken = new DefaultOAuth2AccessToken(token);
            revokedToken.getAdditionalInformation().put("revoked", true);
            revokedToken.setExpiration(EXPIRED_DATE);
            return revokedToken;
        }
        return token;
    }
}
