package de.slauth.auth.server;

import org.junit.Test;

import javax.ws.rs.core.Response;

import static de.slauth.auth.server.support.Assertions.assertThat;
import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN_TYPE;

public class RevocationIT extends BaseIT {

    @Test
    public void test_revoke_access_token() {

        String adminAccessToken = getAccessToken(testUserAdmin);
        String userAccessToken = getAccessToken(testUser);

        assertThat(getMe(userAccessToken)).hasStatusOk();

        Response response = revocations.request()
                .header(AUTHORIZATION, "Bearer " + adminAccessToken)
                .post(entity(userAccessToken, TEXT_PLAIN_TYPE));
        assertThat(response).hasStatusNoContent();

        assertThat(getMe(userAccessToken)).hasStatusUnauthorized();
    }

    private Response getMe(String accessToken) {
        return users.path("/me")
                .request(APPLICATION_JSON_TYPE)
                .header(AUTHORIZATION, "Bearer " + accessToken)
                .get();
    }
}
