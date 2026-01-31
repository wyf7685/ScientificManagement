package com.achievement.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

import com.achievement.utils.KeycloakJwtAuthenticationConverter;

import lombok.AllArgsConstructor;

@Configuration
@AllArgsConstructor
public class SecurityConfig {
    private final KeycloakConfig keycloakConfig;

    @Bean
    protected SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwkSetUri(keycloakConfig.getJwkSetUri())
                                .jwtAuthenticationConverter(
                                        new KeycloakJwtAuthenticationConverter(keycloakConfig.getRealmDefaultRoles()))))
                .authorizeHttpRequests(requests -> {
                    requests.requestMatchers("/api/v1/process-system/**").permitAll();
                    // 其他请求配置...
                    // requests.requestMatchers("xxx").hasRole("YYY");
                    requests.anyRequest().authenticated();
                });
        return httpSecurity.build();
    }
}
