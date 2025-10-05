package com.project.financeapi.util;

import com.project.financeapi.dto.util.JwtPayload;
import com.project.financeapi.entity.User;
import com.project.financeapi.exception.InvalidJwtException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    private final Key key;
    private final long expiration;

    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration:86400000}") long expiration
    ) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("JWT_SECRET não foi definido no ambiente!");
        }

        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.expiration = expiration;
    }

    public String generateToken(User user) {
        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("id", user.getId())
                .claim("tokenVersion", user.getTokenVersion())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims validateToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

        } catch (ExpiredJwtException e) {
            throw new InvalidJwtException(HttpStatus.UNAUTHORIZED, "O token expirou, faça um novo login.");
        } catch (MalformedJwtException e) {
            throw new InvalidJwtException(HttpStatus.BAD_REQUEST, "O token está mal formatado.");
        } catch (SignatureException e) {
            throw new InvalidJwtException(HttpStatus.FORBIDDEN, "A assinatura do token é inválida.");
        } catch (IllegalArgumentException e) {
            throw new InvalidJwtException(HttpStatus.BAD_REQUEST, "O token não pode ser vazio.");
        }
    }

    public JwtPayload extractPayload(String token) {
        Claims claims = validateToken(token);
        String email = claims.getSubject();
        String id = claims.get("id", String.class);
        Integer tokenVersion = claims.get("tokenVersion", Integer.class);
        return new JwtPayload(id, email, tokenVersion);
    }
}
