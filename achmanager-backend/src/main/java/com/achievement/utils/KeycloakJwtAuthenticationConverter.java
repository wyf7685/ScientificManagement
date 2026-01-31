package com.achievement.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

/**
 * Keycloak JWT 认证转换器
 * 
 * 用于从 Keycloak 的 JWT Token 中提取用户信息和角色，并转换为 Spring Security 能够识别的权限
 * 
 * @author research-team
 */
public class KeycloakJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
    private String keycloakRealmDefaultRoles;

    public KeycloakJwtAuthenticationConverter(String keycloakRealmDefaultRoles) {
        this.keycloakRealmDefaultRoles = keycloakRealmDefaultRoles;
    }

    /**
     * 将 JWT Token 转换为认证对象
     * 
     * 从 JWT 的 resource_access 声明中提取客户端角色
     * 
     * @param jwt JWT Token
     * @return 认证 token
     */
    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
        return new JwtAuthenticationToken(jwt, authorities);
    }

    /**
     * 从 JWT 中提取权限
     * 
     * Keycloak 的 JWT Token 中, realm_access.roles 包含 Realm 级别的角色
     * 
     * @param jwt JWT Token
     * @return 权限集合
     */
    @SuppressWarnings("unchecked")
    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        // 首先尝试从标准的 scope 声明中提取权限
        Collection<GrantedAuthority> authorities = jwtGrantedAuthoritiesConverter.convert(jwt);

        if (authorities == null || authorities.isEmpty()) {
            authorities = new ArrayList<>();
        } else {
            authorities = new ArrayList<>(authorities);
        }

        // 从 realm_access 中提取 Realm 级别的角色
        Object realmAccess = jwt.getClaim("realm_access");
        if (realmAccess instanceof Map) {
            Object roles = ((Map<String, Object>) realmAccess).get("roles");

            if (roles instanceof List) {
                for (String role : (List<String>) roles) {
                    if (!isInternalRole(role)) {
                        // 添加 ROLE_ 前缀以便与 Spring Security 的权限检查兼容
                        authorities.add(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
                    }
                }
            }
        }
        return authorities;
    }

    public boolean isInternalRole(String roleName) {
        return keycloakRealmDefaultRoles.contains(roleName);
    }
}
