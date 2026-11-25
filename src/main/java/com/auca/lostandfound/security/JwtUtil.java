package com.auca.lostandfound.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

/**
 * Creates and validates JWT tokens for the Android client.
 *
 * Token lifetime: 24 hours.
 * Claims embedded: userId, email, role.
 */
@Component
public class JwtUtil {

    @Value("${app.jwt.secret:campus-lost-found-default-secret-key-32chars!!}")
    private String secret;

    private static final long EXPIRY_MS = 24 * 60 * 60 * 1000L; // 24 hours

    private Key signingKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /** Generate a signed JWT for the given user. */
    public String generateToken(Long userId, String email, String role) {
        return Jwts.builder()
                .setSubject(email)
                .claim("userId", userId)
                .claim("role",   role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRY_MS))
                .signWith(signingKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /** Parse and validate a token; return its claims. Throws on invalid/expired. */
    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /** Extract the email (subject) from a token without full validation. */
    public String extractEmail(String token) {
        return parseToken(token).getSubject();
    }

    public boolean isValid(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
