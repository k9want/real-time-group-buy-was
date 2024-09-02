package com.realtimegroupbuy.rtgb.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenUtils {

    private final Key key;
    private final long expiredTimeMs;

    public JwtTokenUtils(@Value("${jwt.secret-key}") String secretKey,
        @Value("${jwt.token.expired-time-ms}") long expiredTimeMs) {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.expiredTimeMs = expiredTimeMs;
    }

    public String generateToken(String userName) {
        Claims claims = Jwts.claims();
        claims.put("userName", userName);

        return Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + expiredTimeMs))
            .signWith(key, SignatureAlgorithm.HS256)
            .compact();
    }

    public boolean isTokenExpired(String token) {
        Date expiredDate = extractClaims(token).getExpiration();
        return expiredDate.before(new Date());
    }

    public String getUserName(String token) {
        return extractClaims(token).get("userName", String.class);
    }

    private Claims extractClaims(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .getBody();
    }
}