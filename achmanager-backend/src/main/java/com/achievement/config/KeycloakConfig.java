package com.achievement.config;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "keycloak")
public class KeycloakConfig {
    private String authServerUrl;
    private String realm;
    private String clientId;
    private String clientSecret;
    private String realmDefaultRoles;

    public String getJwkSetUri() {
        return authServerUrl + "/realms/" + realm + "/protocol/openid-connect/certs";
    }

    @Bean
    public Keycloak keycloak() {
        return KeycloakBuilder.builder()
                .serverUrl(authServerUrl)
                .realm(realm)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .build();
    }

    public boolean isDefaultRole(String role) {
        return role != null && realmDefaultRoles.contains(role);
    }
}
