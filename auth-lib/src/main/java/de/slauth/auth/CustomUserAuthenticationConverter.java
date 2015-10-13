package de.slauth.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.token.DefaultUserAuthenticationConverter;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class CustomUserAuthenticationConverter extends DefaultUserAuthenticationConverter {

    private static final Logger log = LoggerFactory.getLogger(CustomUserAuthenticationConverter.class);

    private static final String USER = "user";

    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Map<String, ?> convertUserAuthentication(Authentication authentication) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.putAll(super.convertUserAuthentication(authentication));
        if (authentication.getPrincipal() instanceof User) {
            User user = (User) authentication.getPrincipal();
            log.trace("Adding user {}", user);
            response.put(USER, user);
        }
        if (authentication.getDetails() instanceof Map) {
            Map details = (Map) authentication.getDetails();
            response.put("api_key", details.get("client_id"));
        }
        return response;
    }

    @Override
    public Authentication extractAuthentication(Map<String, ?> map) {
        Authentication authentication = super.extractAuthentication(map);
        if (authentication == null) {
            return null;
        }
        if (map.containsKey(USER)) {
            try {
                String json = asJson(map.get(USER));
                User principal = fromJson(json);
                log.trace("Extracted user {}", principal);
                return new UsernamePasswordAuthenticationToken(
                        principal,
                        authentication.getCredentials(),
                        authentication.getAuthorities()
                );
            } catch (Exception e) {
                log.warn(e.getMessage(), e);
            }
        }
        return authentication;
    }

    private String asJson(Object user) {
        try {
            return objectMapper.writeValueAsString(user);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(String.format("Error converting '%s' to JSON string", user), e);
        }
    }

    private User fromJson(String json) {
        try {
            return objectMapper.readValue(json, User.class);
        } catch (IOException e) {
            throw new RuntimeException(String.format("Error converting from JSON to User object (JSON was: '%s')", json), e);
        }
    }
}
