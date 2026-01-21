package com.achievement.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 *
 * @author system
 * @since 2026-01-21
 */
@Slf4j
@Component
public class JwtUtil {

    /**
     * 密钥 (生产环境应从配置文件读取)
     */
    private static final String SECRET_KEY = "AchievementManagerSecretKey2026ForJWT";

    /**
     * 过期时间：7天（毫秒）
     */
    private static final long EXPIRATION_TIME = 7 * 24 * 60 * 60 * 1000L;

    /**
     * 短期过期时间：1天（毫秒）- 用于未勾选"记住我"的场景
     */
    private static final long SHORT_EXPIRATION_TIME = 24 * 60 * 60 * 1000L;

    /**
     * 生成密钥
     */
    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成JWT令牌
     *
     * @param userId   用户ID
     * @param username 用户名
     * @param role     角色
     * @param remember 是否记住登录
     * @return JWT令牌
     */
    public String generateToken(Integer userId, String username, String role, Boolean remember) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("role", role);

        long expirationTime = Boolean.TRUE.equals(remember) ? EXPIRATION_TIME : SHORT_EXPIRATION_TIME;
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expirationTime);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(getSecretKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 解析JWT令牌
     *
     * @param token JWT令牌
     * @return Claims对象
     */
    public Claims parseToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSecretKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            log.warn("JWT令牌已过期: {}", e.getMessage());
            throw new RuntimeException("登录已过期，请重新登录");
        } catch (UnsupportedJwtException e) {
            log.error("不支持的JWT令牌: {}", e.getMessage());
            throw new RuntimeException("无效的令牌格式");
        } catch (MalformedJwtException e) {
            log.error("JWT令牌格式错误: {}", e.getMessage());
            throw new RuntimeException("令牌格式错误");
        } catch (SignatureException e) {
            log.error("JWT签名验证失败: {}", e.getMessage());
            throw new RuntimeException("令牌签名验证失败");
        } catch (IllegalArgumentException e) {
            log.error("JWT令牌为空: {}", e.getMessage());
            throw new RuntimeException("令牌不能为空");
        }
    }

    /**
     * 从令牌中获取用户ID
     *
     * @param token JWT令牌
     * @return 用户ID
     */
    public Integer getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("userId", Integer.class);
    }

    /**
     * 从令牌中获取用户名
     *
     * @param token JWT令牌
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.getSubject();
    }

    /**
     * 从令牌中获取角色
     *
     * @param token JWT令牌
     * @return 角色
     */
    public String getRoleFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("role", String.class);
    }

    /**
     * 验证令牌是否有效
     *
     * @param token JWT令牌
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
