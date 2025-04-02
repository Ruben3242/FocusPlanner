package com.example.tfg.security.Jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import com.example.tfg.model.User;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    private static final String SECRET_KEY = "tU9YpM4/7cq8+X5H2lRzVm9PXNnlgd0ApOedXv4OqzI=";

    /**
     * Genera un token JWT con información básica del usuario
     */
    public String getToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", user.getId());
        claims.put("role", user.getRole());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getEmail())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24)) // 24 horas
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Obtiene la clave secreta para firmar los tokens
     */
    private Key getKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Extrae el email/username del token (subject)
     */
    public String getEmailFromToken(String token) {
        return getClaim(token, Claims::getSubject);
    }

    /**
     * Extrae el ID del usuario del token
     */
    public Long getUserIdFromToken(String token) {
        return getClaim(token, claims -> claims.get("id", Long.class));
    }

    /**
     * Extrae el rol del usuario del token
     */
    public String getUserRoleFromToken(String token) {
        return getClaim(token, claims -> claims.get("role", String.class));
    }

    /**
     * Verifica si el token es válido para un usuario dado
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = getEmailFromToken(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    /**
     * Extrae todos los claims de un token
     */
    private Claims getAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Extrae un claim específico del token
     */
    public <T> T getClaim(String token, Function<Claims, T> claimResolver) {
        final Claims claims = getAllClaims(token);
        return claimResolver.apply(claims);
    }

    /**
     * Obtiene la fecha de expiración del token
     */
    private Date getExpiration(String token) {
        return getClaim(token, Claims::getExpiration);
    }

    /**
     * Verifica si el token ha expirado
     */
    private boolean isTokenExpired(String token) {
        return getExpiration(token).before(new Date());
    }
}
