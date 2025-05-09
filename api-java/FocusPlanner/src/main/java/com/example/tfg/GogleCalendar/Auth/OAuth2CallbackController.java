//package com.example.tfg.GogleCalendar.Auth;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.*;
//import org.springframework.http.converter.FormHttpMessageConverter;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.client.RestTemplate;
//
//import java.util.HashMap;
//import java.util.Map;
//
//@Controller
//public class OAuth2CallbackController {
//
//    @Value("${google.client.id}")
//    private String clientId;
//
//    @Value("${google.client.secret}")
//    private String clientSecret;
//
//    @Value("${google.redirect.uri}")
//    private String redirectUri;
//
//    private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";
//
//    @GetMapping("/oauth2/callback/google")
//    public String oauth2Callback(@RequestParam("code") String code, @RequestParam("state") String state) {
//        // Logs para depuración
//        System.out.println("Received state: " + state);
//        System.out.println("Received code: " + code);
//
//        // Verificar si el código está vacío o nulo
//        if (code == null || code.isEmpty()) {
//            System.out.println("Error: Código de autorización no recibido");
//            return "error"; // Redirigir a la página de error si no se recibe el código
//        }
//
//        // Intercambiar el código por un token de acceso
//        String accessToken = exchangeCodeForAccessToken(code);
//
//        if (accessToken != null) {
//            System.out.println("Access token obtenido: " + accessToken);
//            // Almacenar el token y redirigir al usuario a la página de inicio
//            return "redirect:/home"; // O la vista que quieras mostrar después de la autenticación
//        } else {
//            System.out.println("Error: No se pudo obtener el token de acceso");
//            return "error"; // En caso de que no se obtenga el token
//        }
//    }
//
//    private String exchangeCodeForAccessToken(String code) {
//        // Configurar los parámetros para la solicitud POST
//        Map<String, String> requestParams = new HashMap<>();
//        requestParams.put("code", code);
//        requestParams.put("client_id", clientId);
//        requestParams.put("client_secret", clientSecret);
//        requestParams.put("redirect_uri", redirectUri);
//        requestParams.put("grant_type", "authorization_code");
//
//        // Crear un RestTemplate y agregar un FormHttpMessageConverter
//        RestTemplate restTemplate = new RestTemplate();
//        restTemplate.getMessageConverters().add(new FormHttpMessageConverter());
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
//        HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestParams, headers);
//
//        try {
//            ResponseEntity<Map> response = restTemplate.exchange(TOKEN_URL, HttpMethod.POST, entity, Map.class);
//
//            if (response.getStatusCode() == HttpStatus.OK) {
//                Map<String, String> responseBody = response.getBody();
//                if (responseBody != null && responseBody.containsKey("access_token")) {
//                    // El token de acceso está en la respuesta
//                    return responseBody.get("access_token");
//                } else {
//                    System.out.println("Error: No se encontró el access_token en la respuesta");
//                }
//            } else {
//                System.out.println("Error: Respuesta de Google no fue OK, código de estado: " + response.getStatusCode());
//            }
//        } catch (Exception e) {
//            System.out.println("Excepción al hacer la solicitud: " + e.getMessage());
//        }
//
//        return null; // Si algo salió mal
//    }
//
//}
