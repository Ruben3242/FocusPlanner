package com.example.tfg.GogleCalendar.Auth.Permisos;

import com.example.tfg.model.User;
import com.example.tfg.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
public class GoogleOAuthService {

    @Value("${google.client.id}")
    private String clientId;

    @Value("${google.client.secret}")
    private String clientSecret;

    private final UserRepository userRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public GoogleOAuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public String getValidAccessToken(User user) throws IOException {
        if (user.getGoogleAccessToken() == null || tokenExpired(user)) {
            return refreshAccessToken(user);
        }
        return user.getGoogleAccessToken();
    }

    private boolean tokenExpired(User user) {
        Instant expiresAt = user.getGoogleAccessTokenExpiry();
        return expiresAt == null || Instant.now().isAfter(expiresAt.minusSeconds(60)); // margen de seguridad
    }

    private String refreshAccessToken(User user) throws IOException {
        if (user.getGoogleRefreshToken() == null) {
            throw new RuntimeException("No refresh token disponible para el usuario.");
        }

        String url = "https://oauth2.googleapis.com/token";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        Map<String, String> params = new HashMap<>();
        params.put("client_id", clientId);
        params.put("client_secret", clientSecret);
        params.put("refresh_token", user.getGoogleRefreshToken());
        params.put("grant_type", "refresh_token");

        HttpEntity<Map<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("No se pudo refrescar el token: " + response.getBody());
        }

        JsonNode json = objectMapper.readTree(response.getBody());
        String newAccessToken = json.get("access_token").asText();
        int expiresIn = json.get("expires_in").asInt();

        // Guardar en el usuario
        user.setGoogleAccessToken(newAccessToken);
        user.setGoogleAccessTokenExpiry(Instant.now().plusSeconds(expiresIn));
        userRepository.save(user); // persistimos el nuevo token

        return newAccessToken;
    }
}
