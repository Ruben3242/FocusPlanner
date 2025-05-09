package com.example.tfg.GogleCalendar.Auth.Permisos;

import com.example.tfg.model.User;
import com.example.tfg.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/google")
public class GoogleOAuthController {

    @Value("${google.client.id}")
    private String clientId;

    @Value("${google.client.secret}")
    private String clientSecret;

    private final UserRepository userRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public GoogleOAuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    @GetMapping("/callback")
    public ResponseEntity<String> handleGoogleCallback(@RequestParam("code") String code,
                                                       @RequestParam("state") Long userId) {
        try {
            // 1. Intercambiar código por tokens
            String tokenUrl = "https://oauth2.googleapis.com/token";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            Map<String, String> params = new HashMap<>();
            params.put("code", code);
            params.put("client_id", clientId);
            params.put("client_secret", clientSecret);
            params.put("grant_type", "authorization_code");

            System.out.println("Código recibido: " + code);
            System.out.println("Usuario asociado: " + userId);

            HttpEntity<Map<String, String>> request = new HttpEntity<>(params, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(tokenUrl, request, String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                return ResponseEntity.status(400).body("Error al obtener tokens");
            }

            JsonNode json = objectMapper.readTree(response.getBody());

            String accessToken = json.get("access_token").asText();
            String refreshToken = json.has("refresh_token") ? json.get("refresh_token").asText() : null;
            int expiresIn = json.get("expires_in").asInt();

            // 2. Guardar en el usuario
            User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            user.setGoogleAccessToken(accessToken);
            user.setGoogleRefreshToken(refreshToken);
            user.setGoogleAccessTokenExpiry(Instant.now().plusSeconds(expiresIn));
            userRepository.save(user);

            return ResponseEntity.ok("Cuenta de Google vinculada correctamente");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error al vincular cuenta de Google");
        }
    }
    @GetMapping("/authorize")
    public void redirectToGoogle(HttpServletResponse response, @RequestParam("userId") Long userId) throws IOException {
        String redirectUri = "http://localhost:8080/api/google/callback"; // Asegúrate de que coincida con Google Cloud Console

        String authUrl = "https://accounts.google.com/o/oauth2/v2/auth"
                + "?client_id=" + clientId
                + "&redirect_uri=" + redirectUri
                + "&response_type=code"
                + "&scope=https://www.googleapis.com/auth/calendar"
                + "&access_type=offline"
                + "&prompt=consent"
                + "&state=" + userId;

        response.sendRedirect(authUrl);
    }

}
