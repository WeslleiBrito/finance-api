package com.project.financeapi.util;

import com.project.financeapi.dto.util.JwtPayload;
import com.project.financeapi.entity.User;
import com.project.financeapi.exception.InvalidJwtException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import io.github.cdimascio.dotenv.Dotenv;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {
    private static final Dotenv dotenv = Dotenv.load();
    private static final String secret = dotenv.get("JWT_SECRET");
    private static final long expiration = Long.parseLong(dotenv.get("JWT_EXPIRATION"));
    private static final Key key = Keys.hmacShaKeyFor(secret.getBytes());

    public static String generateToken(User user) {
        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("id", user.getId())
                .claim("tokenVersion", user.getTokenVersion())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public static Claims validateToken(String token) {
        try {

            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

        }catch (ExpiredJwtException e) {
            throw new InvalidJwtException(HttpStatus.UNAUTHORIZED, "O token expirou, faça um novo login.");
        }catch (MalformedJwtException e) {
            throw new InvalidJwtException(HttpStatus.BAD_REQUEST, "O token está mal formatado.");
        }catch (SignatureException  e) {
            throw new InvalidJwtException(HttpStatus.FORBIDDEN, "A assinatura do token é inválida.");
        }catch (IllegalArgumentException  e) {
            throw new InvalidJwtException(HttpStatus.BAD_REQUEST, "O token não pode ser vazio.");
        }

    }

    public static JwtPayload extractPayload(String token) {
        Claims claims = validateToken(token);
        String email = claims.getSubject();
        String id = claims.get("id", String.class);
        Integer tokenVersion = claims.get("tokenVersion", Integer.class);
        return new JwtPayload(id, email, tokenVersion);
    }
}
