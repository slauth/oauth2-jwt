package de.slauth.auth.server;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.google.common.collect.ImmutableSet;
import de.slauth.auth.server.support.OAuthTokenResponseFormat;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static de.slauth.auth.server.support.Assertions.assertThat;
import static javax.ws.rs.client.Entity.entity;

@ActiveProfiles("it")
@WebIntegrationTest(randomPort = true)
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
public abstract class BaseIT {

    protected ClientEntity testClient;
    protected UserEntity testUser;
    protected UserEntity testUserAdmin;

    protected WebTarget root;
    protected WebTarget oauthToken;
    protected WebTarget users;
    protected WebTarget revocations;

    @Autowired ClientRepo clientRepo;
    @Autowired UserRepo userRepo;

    @Value("${local.server.port}")
    int port;

    @Before
    public void setup() {
        initTestClients();
        initTestUsers();
        initJerseyClient();
    }

    protected String getAccessToken(UserEntity user) {
        Form form = new Form()
                .param("grant_type", "password")
                .param("client_id", testClient.getId())
                .param("client_secret", testClient.getSecret())
                .param("username", user.getUsername())
                .param("password", user.getPassword());
        Response response = oauthToken
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));
        assertThat(response).hasStatusOk();
        OAuthTokenResponseFormat entity = response.readEntity(OAuthTokenResponseFormat.class);
        assertThat(entity).containsAccessToken();
        return entity.getAccessToken();
    }

    private void initTestClients() {
        testClient = new ClientEntity()
                .setId("test_client")
                .setSecret("test_client_secret");
        clientRepo.deleteAll();
        clientRepo.save(testClient);
    }

    private void initTestUsers() {
        testUser = new UserEntity()
                .setUsername("test_user")
                .setPassword("test_user_password")
                .setScopes("read,write");
        testUserAdmin = new UserEntity()
                .setUsername("test_user_admin")
                .setPassword("test_user_admin_password")
                .setScopes("admin");
        userRepo.deleteAll();
        userRepo.save(ImmutableSet.of(testUser, testUserAdmin));
    }

    private void initJerseyClient() {
        root = ClientBuilder.newClient().register(JacksonJsonProvider.class).target("http://localhost:" + port);
        oauthToken = root.path("/oauth/token");
        users = root.path("/users");
        revocations = root.path("/revocations");
    }
}
