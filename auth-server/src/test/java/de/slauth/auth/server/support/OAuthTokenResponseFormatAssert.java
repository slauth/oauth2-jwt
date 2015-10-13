package de.slauth.auth.server.support;

import org.assertj.core.api.AbstractAssert;

public class OAuthTokenResponseFormatAssert extends AbstractAssert<OAuthTokenResponseFormatAssert, OAuthTokenResponseFormat> {

    protected OAuthTokenResponseFormatAssert(OAuthTokenResponseFormat actual) {
        super(actual, OAuthTokenResponseFormatAssert.class);
    }

    public OAuthTokenResponseFormatAssert containsAccessToken() {
        isNotNull();
        if (actual.getAccessToken() == null) {
            failWithMessage("Expected response to contain an access_token, but it did not");
        }
        return this;
    }

    public OAuthTokenResponseFormatAssert containsRefreshToken() {
        isNotNull();
        if (actual.getRefreshToken() == null) {
            failWithMessage("Expected response to contain a refresh_token, but it did not");
        }
        return this;
    }
}
