package com.example.tfg.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys; // Importa la clase Keys para generar claves seguras
import javax.crypto.SecretKey; // Importa la clase SecretKey para gestionar claves seguras
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    // Método para generar el token JWT
    public String generateToken(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();  // Obtener detalles del usuario autenticado

        // Generar el token JWT
        return Jwts.builder()
                .setSubject(userDetails.getUsername())  // Establecer el nombre de usuario como "subject"
                .setIssuedAt(new Date())  // Establecer la fecha de emisión
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))  // Establecer la fecha de expiración
                .signWith(getSigningKey())  // Usar la clave generada o configurada
                .compact();
    }

    // Método para obtener el nombre de usuario desde el token
    public String getUsernameFromToken(String token) {
        return Jwts.parserBuilder()  // Usar parserBuilder en lugar de parser
                .setSigningKey(getSigningKey())  // Establecer la clave secreta para validar el token
                .build()
                .parseClaimsJws(token)  // Parsear el token JWT
                .getBody()
                .getSubject();  // Obtener el nombre de usuario
    }

    // Método para validar el token JWT
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()  // Usar parserBuilder en lugar de parser
                    .setSigningKey(getSigningKey())  // Establecer la clave secreta
                    .build()
                    .parseClaimsJws(token);  // Intentar parsear el token

            return true;  // Si el token es válido, retornamos true
        } catch (JwtException | IllegalArgumentException e) {
            return false;  // Si el token es inválido, retornamos false
        }
    }

    // Genera automáticamente una clave secreta segura para HS512 si es necesario
    private SecretKey getSigningKey() {
        if (jwtSecret == null || jwtSecret.length() < 512 / 8) {
            // Si la clave secreta es demasiado corta, genera una clave segura
            return Keys.secretKeyFor(SignatureAlgorithm.HS512);
        } else {
            // Usa la clave secreta configurada
            return new javax.crypto.spec.SecretKeySpec(jwtSecret.getBytes(), SignatureAlgorithm.HS512.getJcaName());
        }
    }
}
