package de.slauth.auth.server;

import com.google.common.base.Predicates;
import de.slauth.auth.AuthConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.token.AccessTokenConverter;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.springframework.http.HttpMethod.OPTIONS;

@SpringBootApplication
@EnableSwagger2
@Import(AuthConfig.class)
public class Application {

    public static void main(final String[] args) {
        new SpringApplicationBuilder()
                .sources(Application.class)
                .showBanner(false)
                .run(args);
    }

    @Value("${info.version}")
    private String version;

    @Bean
    public Docket swagger() {
        return new Docket(DocumentationType.SWAGGER_2)
                .useDefaultResponseMessages(false)
                .genericModelSubstitutes(ResponseEntity.class)
                .apiInfo(new ApiInfoBuilder()
                        .title("Auth Service")
                        .description("<p>This service acts as a central source of " +
                                "<strong>auth</strong>entication and <strong>auth</strong>orization based on " +
                                "<a href=\"http://oauth.net/2/\">OAuth 2.0</a> and " +
                                "<a href=\"http://jwt.io/\">JSON Web Tokens</a>.</p>")
                        .version(version)
                        .contact("contact").license("license")
                        .build())
                .select()
                .paths(Predicates.or(
                        PathSelectors.ant("/oauth/token"),
                        PathSelectors.ant("/revocations/**"),
                        PathSelectors.ant("/users/**")
                ))
                .build();
    }

    @Configuration
    @EnableAuthorizationServer
    static class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

        @Autowired
        AuthenticationManager authenticationManager;
        @Autowired
        AccessTokenConverter accessTokenConverter;
        @Autowired
        ClientDetailsService clientDetailsService;
        @Autowired
        TokenStore tokenStore;

        public void configure(final AuthorizationServerSecurityConfigurer security) throws Exception {
            // do not require clients to send HTTP Basic Authentication headers
            security.allowFormAuthenticationForClients();
        }

        public void configure(final ClientDetailsServiceConfigurer clients) throws Exception {
            clients.withClientDetails(clientDetailsService);
        }

        public void configure(final AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
            endpoints.authenticationManager(authenticationManager)
                    .accessTokenConverter(accessTokenConverter)
                    .tokenStore(tokenStore);
        }
    }

    @Configuration
    @EnableResourceServer
    static class ResourceServerConfig extends ResourceServerConfigurerAdapter {

        @Autowired
        TokenStore tokenStore;

        public void configure(final ResourceServerSecurityConfigurer resources) throws Exception {
            resources.resourceId("auth-server")
                    .tokenStore(tokenStore);
        }

        public void configure(final HttpSecurity http) throws Exception {
            http.authorizeRequests()
                    .antMatchers("/internal/**").permitAll()
                    .antMatchers("/v2/api-docs").permitAll()
                    .antMatchers(OPTIONS, "/**").permitAll()
                    .anyRequest().authenticated();
        }
    }

    @Configuration
    static class AuthenticationManagerConfig extends GlobalAuthenticationConfigurerAdapter {

        @Autowired
        UserDetailsService userDetailsService;

        @Override
        public void init(AuthenticationManagerBuilder auth) throws Exception {
            auth.userDetailsService(userDetailsService)
                    // no password encoding at the moment...
                    .passwordEncoder(NoOpPasswordEncoder.getInstance());
        }
    }

    @Component
    static class CorsFilter extends OncePerRequestFilter {

        @Override
        public void doFilterInternal(HttpServletRequest req, HttpServletResponse resp, FilterChain chain)
                throws IOException, ServletException {
            resp.setHeader("Access-Control-Allow-Origin", "*");
            resp.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE, PUT");
            resp.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type");
            chain.doFilter(req, resp);
        }
    }
}
