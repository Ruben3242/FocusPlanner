//package com.example.tfg.security;
//
//import io.jsonwebtoken.Claims;
//import io.jsonwebtoken.Jwts;
//import io.jsonwebtoken.SignatureAlgorithm;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.stereotype.Component;
//
//import java.util.Date;
//import java.util.function.Function;
//
//@Component
//public class JwtTokenProvider {
//
//    @Value("${jwt.secret}")
//    private String secretKey;
//
//    @Value("${jwt.expiration}")
//    private long jwtExpiration;
//
//    // Genera un token JWT
//    public String generateToken(UserDetails userDetails) {
//        return Jwts.builder()
//                .setSubject(userDetails.getUsername())
//                .setIssuedAt(new Date())
//                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
//                .signWith(SignatureAlgorithm.HS256, secretKey)
//                .compact();
//    }
//
//    // Extrae el nombre de usuario del token
//    public String getUsernameFromToken(String token) {
//        return getClaimFromToken(token, Claims::getSubject);
//    }
//
//    // Verifica si el token es válido
//    public boolean validateToken(String token, UserDetails userDetails) {
//        final String username = getUsernameFromToken(token);
//        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
//    }
//
//    private <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
//        Claims claims = Jwts.parser()
//                .setSigningKey(secretKey)
//                .parseClaimsJws(token)
//                .getBody();
//        return claimsResolver.apply(claims);
//    }
//
//    private boolean isTokenExpired(String token) {
//        return getClaimFromToken(token, Claims::getExpiration).before(new Date());
//    }
//}
