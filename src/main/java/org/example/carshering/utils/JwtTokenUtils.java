package org.example.carshering.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.example.carshering.security.ClientDetails;
import org.hibernate.annotations.Comment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;

import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class JwtTokenUtils {

    private final SecretKey secretKey;
    private final Duration jwtLifetime;


    public JwtTokenUtils(@Value("${jwt.secret}") String secret,
                         @Value("${jwt.lifetime}") Duration jwtLifetime) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.jwtLifetime = jwtLifetime;
    }

    public String generateToken(ClientDetails clientDetails) {
        Map<String, Object> claims = new HashMap<>();
        List<String> authorities = clientDetails.getAuthorities()
                .stream().map(GrantedAuthority::getAuthority)
                .toList();

        claims.put("authorities", authorities);
        claims.put("email", clientDetails.getEmail());
        claims.put("id", clientDetails.getId());

        Date issuedAt = new Date();
        Date expiration = new Date(issuedAt.getTime() + jwtLifetime.toMillis());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(clientDetails.getUsername())
                .setIssuedAt(issuedAt)
                .setExpiration(expiration)
                .signWith(secretKey)
                .compact();
    }

    public Claims getClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String getUsernameFromToken(String token) {
        return getClaimsFromToken(token).getSubject();
    }

    public List<SimpleGrantedAuthority> getAuthoritiesFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) claims.get("authorities");
        return roles.stream()
                .map(SimpleGrantedAuthority::new)
                .toList();
    }

    public String getIdFromToken(String token) {
        return getClaimsFromToken(token).get("id").toString();
    }

    public String getEmailFromToken(String token) {
        return getClaimsFromToken(token).get("email").toString();
    }


}