package de.slauth.auth.server;

import de.slauth.auth.User;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import static de.slauth.auth.server.support.Assertions.assertThat;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

public class UserIT extends BaseIT {

    @Test
    public void test_users_me() {
        Response response = users.path("/me")
                .request(APPLICATION_JSON_TYPE)
                .header(AUTHORIZATION, "Bearer " + getAccessToken(testUser))
                .get();
        assertThat(response).hasStatusOk();
        User entity = response.readEntity(User.class);
        assertThat(entity).isNotNull();
        assertThat(entity.getUsername()).isEqualTo(testUser.getUsername());
    }

    @Test
    public void test_users_me_returns_unauthorized_without_token() {
        Response response = users.path("/me")
                .request(APPLICATION_JSON_TYPE)
                .get();
        assertThat(response).hasStatusUnauthorized();
    }

    @Test
    public void test_users_others() {
        // requesting test_user as test_user_admin
        Response response = users.path("{username}")
                .resolveTemplate("username", testUser.getUsername())
                .request(APPLICATION_JSON_TYPE)
                .header(AUTHORIZATION, "Bearer " + getAccessToken(testUserAdmin))
                .get();
        assertThat(response).hasStatusOk();
        User entity = response.readEntity(User.class);
        assertThat(entity).isNotNull();
        assertThat(entity.getUsername()).isEqualTo(testUser.getUsername());
    }

    @Test
    public void test_users_others_returns_forbidden_if_not_admin() {
        Response response = users.path("{username}")
                .resolveTemplate("username", testUser.getUsername())
                .request(APPLICATION_JSON_TYPE)
                .header(AUTHORIZATION, "Bearer " + getAccessToken(testUser))
                .get();
        assertThat(response).hasStatusForbidden();
    }

    @Test
    public void test_create_user() {
        String entity = "{\"username\":\"foo\",\"password\":\"bar\"}";
        Response response = users
                .request(APPLICATION_JSON_TYPE)
                .header(AUTHORIZATION, "Bearer " + getAccessToken(testUserAdmin))
                .post(Entity.entity(entity, APPLICATION_JSON_TYPE));
        assertThat(response).hasStatusCreated();
        assertThat(response.getLocation().getPath()).isEqualTo("/users/foo");
    }
}
