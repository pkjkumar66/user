package com.example.demo.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    @Value("${app.jwt.secret:myDefaultSecretKeyForJWTWhichShouldBeVeryLongAndSecure}")
    private String secret;

    @Value("${app.jwt.expiration:86400}") // in seconds
    private Long expiration;

    @Value("${app.jwt.issuer:myapp}") // issuer claim
    private String issuer;

    @Value("${app.jwt.audience:users}") // audience claim
    private String audience;

    private SecretKey signingKey;

    @PostConstruct
    public void init() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalStateException(
                    "JWT secret must be at least 256 bits (32 bytes). Configure a stronger secret."
            );
        }
        signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(com.example.demo.dao.User user) {
        Map<String, Object> claims = new HashMap<>();
        // custom user info claims
        //        claims.put("userId", user.getId());
        claims.put("email", user.getEmail());
        claims.put("provider", user.getProvider() != null ? user.getProvider() : "local");
        claims.put("username", user.getUsername());

        // standard claims: subject
        String subject = user.getUsername() != null ? user.getUsername() :
                (user.getEmail() != null ? user.getEmail() : user.getId());

        return createToken(claims, subject);
    }

    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expiration * 1000);

        return Jwts.builder()
                .setHeaderParam("typ", "JWT")         // header: type
                .setHeaderParam("alg", "HS256")       // header: algorithm
                .setClaims(claims)                    // payload: custom user info
                .setSubject(subject)                  // payload: sub
                .setIssuer(issuer)                    // payload: iss
                .setAudience(audience)                // payload: aud
                .setIssuedAt(now)                     // payload: iat
                .setExpiration(exp)                   // payload: exp
                .signWith(signingKey, SignatureAlgorithm.HS256) // signature
                .compact();
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username != null &&
                username.equals(userDetails.getUsername()) &&
                !isTokenExpired(token);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }


    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, java.util.function.Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
}
