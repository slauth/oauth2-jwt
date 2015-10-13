package de.slauth.auth;

import com.hazelcast.config.*;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;
import org.springframework.security.oauth2.provider.expression.OAuth2MethodSecurityExpressionHandler;
import org.springframework.security.oauth2.provider.token.DefaultAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.UserAuthenticationConverter;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.List;
import java.util.Map;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class AuthConfig extends GlobalMethodSecurityConfiguration {

    public static final String HAZELCAST_MAP_NAME = "de.slauth.auth.revoked_tokens";

    @Value("${oauth.keystore.location}")
    String keystoreLocation;
    @Value("${oauth.keystore.password}")
    String keystorePassword;
    @Value("${oauth.key.alias}")
    String keyAlias;
    @Value("${oauth.key.password}")
    String keyPassword;

    @Value("#{'${oauth.revocations.cluster.members:localhost}'.split(',')}")
    List<String> revocationsClusterMembers;
    @Value("${oauth.revocations.cluster.port.start:5701}")
    int revocationsClusterPortStart;
    @Value("${oauth.revocations.cluster.port.count:100}")
    int revocationsClusterPortCount;
    @Value("${oauth.revocations.cluster.ttl:604800}") // 7 days
    int revocationsClusterTtl;

    @Override
    protected MethodSecurityExpressionHandler createExpressionHandler() {
        return new OAuth2MethodSecurityExpressionHandler();
    }

    @Bean
    public UserAuthenticationConverter userAuthenticationConverter() {
        return new CustomUserAuthenticationConverter();
    }

    @Bean
    public JwtAccessTokenConverter accessTokenConverter() {
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
        converter.setKeyPair(getKeyPair());
        DefaultAccessTokenConverter accessTokenConverter = new DefaultAccessTokenConverter();
        accessTokenConverter.setUserTokenConverter(userAuthenticationConverter());
        converter.setAccessTokenConverter(accessTokenConverter);
        return converter;
    }

    @Bean
    public TokenStore tokenStore() {
        return new CustomJwtTokenStore(accessTokenConverter());
    }

    @Bean(destroyMethod = "shutdown")
    public HazelcastInstance hazelcast() {
        Config config = new Config();
        NetworkConfig networkConfig = config.getNetworkConfig();
        // configure used ports
        networkConfig.setPort(revocationsClusterPortStart);
        networkConfig.setPortCount(revocationsClusterPortCount);
        // disable multicast
        JoinConfig joinConfig = networkConfig.getJoin();
        joinConfig.getMulticastConfig().setEnabled(false);
        // configure TCP/IP members
        TcpIpConfig tcpIpConfig = joinConfig.getTcpIpConfig().setEnabled(true);
        tcpIpConfig.setMembers(revocationsClusterMembers);
        // configure revocation map
        MapConfig mapConfig = new MapConfig();
        mapConfig.setName(HAZELCAST_MAP_NAME);
        mapConfig.setTimeToLiveSeconds(revocationsClusterTtl);
        config.addMapConfig(mapConfig);
        return Hazelcast.newHazelcastInstance(config);
    }

    @Bean
    public RevocationService revocationService() {
        Map<String, String> revokedTokens = hazelcast().getMap(HAZELCAST_MAP_NAME);
        return new RevocationService(revokedTokens);
    }

    private KeyPair getKeyPair() {
        try (InputStream stream = new FileInputStream(keystoreLocation)) {
            KeyStore keyStore = KeyStore.getInstance("jks");
            keyStore.load(stream, keystorePassword.toCharArray());
            PrivateKey privateKey = (PrivateKey) keyStore.getKey(keyAlias, keyPassword.toCharArray());
            if (privateKey == null) {
                throw new AuthConfigException("No private key with alias '" + keyAlias + "' exists");
            }
            Certificate certificate = keyStore.getCertificate(keyAlias);
            if (certificate == null) {
                throw new AuthConfigException("No certificate with alias '" + keyAlias + "' exists");
            }
            PublicKey publicKey = certificate.getPublicKey();
            if (publicKey == null) {
                throw new AuthConfigException("No public key with alias '" + keyAlias + "' exists");
            }
            return new KeyPair(publicKey, privateKey);
        } catch (Exception e) {
            throw new AuthConfigException("Error loading key pair", e);
        }
    }
}
