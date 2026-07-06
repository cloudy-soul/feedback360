package com.feedback.feedback360.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {
    private final SecretKey key;
    private final long expirationMinutes;

    public JwtUtil(@Value("${app.jwt.secret}") String secret,
                   @Value("${app.jwt.expiration-minutes}") long expirationMinutes) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMinutes = expirationMinutes;
    }

    public String generateToken(String email, String role, Long userId) {
        Date now = new Date();
        return Jwts.builder()
                .subject(email).claim("role", role).claim("userId", userId)
                .issuedAt(now).expiration(new Date(now.getTime() + expirationMinutes * 60_000))
                .signWith(key, Jwts.SIG.HS256).compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }

    public boolean isValid(String token) {
        try { parseClaims(token); return true; } catch (Exception e) { return false; }
    }
}
