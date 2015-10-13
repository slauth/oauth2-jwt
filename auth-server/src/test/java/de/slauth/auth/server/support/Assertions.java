package de.slauth.auth.server.support;

import javax.ws.rs.core.Response;

public class Assertions extends org.assertj.core.api.Assertions {

    public static ResponseAssert assertThat(Response actual) {
        return new ResponseAssert(actual);
    }

    public static OAuthTokenResponseFormatAssert assertThat(OAuthTokenResponseFormat actual) {
        return new OAuthTokenResponseFormatAssert(actual);
    }
}
