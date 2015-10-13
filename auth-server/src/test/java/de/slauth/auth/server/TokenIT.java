package de.slauth.auth.server;

import de.slauth.auth.server.support.OAuthTokenResponseFormat;
import org.junit.Test;

import javax.ws.rs.core.Form;
import javax.ws.rs.core.Response;

import static de.slauth.auth.server.support.Assertions.assertThat;
import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

public class TokenIT extends BaseIT {

    @Test
    public void test_grant_type_password_success() {
        Response passwordResponse = requestToken(passwordForm());
        assertThat(passwordResponse).hasStatusOk();
        OAuthTokenResponseFormat passwordEntity = passwordResponse.readEntity(OAuthTokenResponseFormat.class);
        assertThat(passwordEntity).containsAccessToken();
    }

    @Test
    public void test_grant_type_password_unknown_client() {
        Form form = new Form()
                .param("grant_type", "password")
                .param("client_id", "foo")
                .param("client_secret", testClient.getSecret())
                .param("username", "foo")
                .param("password", testUser.getPassword());
        Response passwordResponse = requestToken(form);
        assertThat(passwordResponse).hasStatusUnauthorized();
    }

    @Test
    public void test_grant_type_password_invalid_client_secret() {
        Form form = new Form()
                .param("grant_type", "password")
                .param("client_id", testClient.getId())
                .param("client_secret", "foo")
                .param("username", testUser.getUsername())
                .param("password", testUser.getPassword());
        Response passwordResponse = requestToken(form);
        assertThat(passwordResponse).hasStatusUnauthorized();
    }

    @Test
    public void test_grant_type_password_unknown_user() {
        Form form = new Form()
                .param("grant_type", "password")
                .param("client_id", testClient.getId())
                .param("client_secret", testClient.getSecret())
                .param("username", "foo")
                .param("password", testUser.getPassword());
        Response passwordResponse = requestToken(form);
        assertThat(passwordResponse).hasStatusBadRequest();
    }

    @Test
    public void test_grant_type_password_invalid_password() {
        Form form = new Form()
                .param("grant_type", "password")
                .param("client_id", testClient.getId())
                .param("client_secret", testClient.getSecret())
                .param("username", testUser.getUsername())
                .param("password", "foo");
        Response passwordResponse = requestToken(form);
        assertThat(passwordResponse).hasStatusBadRequest();
    }

    @Test
    public void test_grant_type_refresh_token_success() {

        // 1. grant_type=password
        String refreshToken = getRefreshToken();

        // 2. grant_type=refresh_token
        Response refreshResponse = requestToken(refreshForm(refreshToken));
        assertThat(refreshResponse).hasStatusOk();
        OAuthTokenResponseFormat refreshEntity = refreshResponse.readEntity(OAuthTokenResponseFormat.class);
        assertThat(refreshEntity).containsAccessToken();
    }

    @Test
    public void test_grant_type_refresh_token_unknown_client() {

        // 1. grant_type=password
        String refreshToken = getRefreshToken();

        // 2. grant_type=refresh_token
        Form form = new Form()
                .param("grant_type", "refresh_token")
                .param("client_id", "foo")
                .param("client_secret", testClient.getSecret())
                .param("refresh_token", refreshToken);
        Response refreshResponse = requestToken(form);
        assertThat(refreshResponse).hasStatusUnauthorized();
    }

    @Test
    public void test_grant_type_refresh_token_invalid_client_secret() {

        // 1. grant_type=password
        String refreshToken = getRefreshToken();

        // 2. grant_type=refresh_token
        Form form = new Form()
                .param("grant_type", "refresh_token")
                .param("client_id", testClient.getId())
                .param("client_secret", "foo")
                .param("refresh_token", refreshToken);
        Response refreshResponse = requestToken(form);
        assertThat(refreshResponse).hasStatusUnauthorized();
    }

    private String getRefreshToken() {
        Response passwordResponse = requestToken(passwordForm());
        assertThat(passwordResponse).hasStatusOk();
        OAuthTokenResponseFormat passwordEntity = passwordResponse.readEntity(OAuthTokenResponseFormat.class);
        assertThat(passwordEntity).containsRefreshToken();
        return passwordEntity.getRefreshToken();
    }

    private Form passwordForm() {
        return new Form()
                .param("grant_type", "password")
                .param("client_id", testClient.getId())
                .param("client_secret", testClient.getSecret())
                .param("username", testUser.getUsername())
                .param("password", testUser.getPassword());
    }

    private Form refreshForm(String refreshToken) {
        return new Form()
                .param("grant_type", "refresh_token")
                .param("client_id", testClient.getId())
                .param("client_secret", testClient.getSecret())
                .param("refresh_token", refreshToken);
    }

    private Response requestToken(Form form) {
        return oauthToken
                .request(APPLICATION_JSON_TYPE)
                .post(entity(form, APPLICATION_FORM_URLENCODED_TYPE));
    }
}
