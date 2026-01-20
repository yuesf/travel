package com.travel.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 * 
 * @author travel-platform
 */
@Component
public class JwtUtil {
    
    @Value("${travel.jwt.secret}")
    private String secret;
    
    @Value("${travel.jwt.expiration}")
    private Long expiration;
    
    /**
     * 获取安全的密钥
     * 使用SHA-256哈希确保密钥长度至少为256位（32字节）
     */
    private SecretKey getSecretKey() {
        try {
            byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
            // 如果密钥长度不足32字节，使用SHA-256哈希扩展
            if (keyBytes.length < 32) {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                keyBytes = digest.digest(keyBytes);
            }
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("无法初始化SHA-256算法", e);
        }
    }
    
    /**
     * 生成Token（后台管理）
     */
    public String generateToken(Long userId, String username) {
        return generateToken(userId, username, "admin");
    }
    
    /**
     * 生成Token（支持指定类型）
     */
    public String generateToken(Long userId, String username, String tokenType) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("tokenType", tokenType);
        return createToken(claims);
    }
    
    /**
     * 创建Token
     */
    private String createToken(Map<String, Object> claims) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);
        
        SecretKey key = getSecretKey();
        
        return Jwts.builder()
                .claims(claims)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }
    
    /**
     * 从Token中获取Claims
     */
    public Claims getClaimsFromToken(String token) {
        try {
            SecretKey key = getSecretKey();
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 从Token中获取用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        if (claims != null) {
            Object userId = claims.get("userId");
            if (userId instanceof Integer) {
                return ((Integer) userId).longValue();
            } else if (userId instanceof Long) {
                return (Long) userId;
            }
        }
        return null;
    }
    
    /**
     * 从Token中获取用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims != null ? claims.get("username", String.class) : null;
    }
    
    /**
     * 验证Token是否有效
     */
    public boolean validateToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims != null && !isTokenExpired(claims);
    }
    
    /**
     * 检查Token是否过期
     */
    private boolean isTokenExpired(Claims claims) {
        Date expiration = claims.getExpiration();
        return expiration.before(new Date());
    }
    
    /**
     * 获取Token过期时间
     */
    public Date getExpirationDateFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims != null ? claims.getExpiration() : null;
    }
    
    /**
     * 从Token中获取token类型
     */
    public String getTokenType(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims != null ? claims.get("tokenType", String.class) : null;
    }
}
