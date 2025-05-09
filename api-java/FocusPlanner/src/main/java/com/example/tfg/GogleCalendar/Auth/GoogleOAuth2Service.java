//package com.example.tfg.GogleCalendar.Auth;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.*;
//import org.springframework.stereotype.Service;
//import org.springframework.util.LinkedMultiValueMap;
//import org.springframework.util.MultiValueMap;
//import org.springframework.web.client.RestTemplate;
//
//import java.util.Map;
//
//@Service
//public class GoogleOAuth2Service {
//    @Value("${google.client.id}")
//    private String clientId;
//
//    @Value("${google.client.secret}")
//    private String clientSecret;
//
//    @Value("${google.redirect.uri}")
//    private String redirectUri;
//
//    private final RestTemplate restTemplate;
//
//    public GoogleOAuth2Service(RestTemplate restTemplate) {
//        this.restTemplate = restTemplate;
//    }
//
//    public String exchangeCodeForToken(String authorizationCode) {
//        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
//        params.add("client_id", clientId);
//        params.add("client_secret", clientSecret);
//        params.add("code", authorizationCode);
//        params.add("redirect_uri", redirectUri);
//        params.add("grant_type", "authorization_code");
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
//
//        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(params, headers);
//
//        try {
//            ResponseEntity<Map> response = restTemplate.exchange(
//                    "https://oauth2.googleapis.com/token",
//                    HttpMethod.POST,
//                    requestEntity,
//                    Map.class
//            );
//
//            Map<String, Object> body = response.getBody();
//            return body != null ? (String) body.get("access_token") : null;
//        } catch (Exception e) {
//            System.out.println("Error al obtener el token de acceso: " + e.getMessage());
//        }
//
//        return null;
//    }
//}