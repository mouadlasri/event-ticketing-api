package org.practice.eventticketingapi.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {
    private final long expiration;
    private final SecretKey signingKey;

    public JwtService(@Value("${jwt.secret-key}") String secretKey, @Value("${jwt.expiration}") long expiration) {
        this.signingKey = Keys.hmacShaKeyFor(secretKey.getBytes());
        this.expiration = expiration;
    }

    public String generateToken(UUID userId) {
        Date issuedAt = new Date();
        Date expiration = new Date(issuedAt.getTime() + this.expiration);
        String token = Jwts.builder()
                .subject(userId.toString())
                .issuedAt(issuedAt)
                .expiration(expiration)
                .signWith(this.signingKey)
                .compact();

        return token;
    }

    // validate token and extract the user id from it
    public UUID extractUserId(String token) {
        String id = Jwts.parser()
                .verifyWith(this.signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();

        return UUID.fromString(id);
    }
}
