//package com.example.tfg.GogleCalendar.Auth;
//
//import com.example.tfg.model.User;
//import com.example.tfg.repository.UserRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
//import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
//import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
//import org.springframework.security.oauth2.core.OAuth2AccessToken;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//
//import java.util.Optional;
//
//@Controller
//@RequestMapping("/oauth2")
//@RequiredArgsConstructor
//public class GoogleAuthController {
//
//    private final OAuth2AuthorizedClientService authorizedClientService;
//    private final UserRepository userRepository;
//
//    @GetMapping("/callback")
//    public ResponseEntity<String> oauth2Callback(@RequestParam(value = "code", required = false) String code,
//                                                 Authentication authentication) {
//        if (authentication instanceof OAuth2AuthenticationToken authToken) {
//            OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(
//                    authToken.getAuthorizedClientRegistrationId(), authToken.getName());
//
//            if (authorizedClient != null) {
//                OAuth2AccessToken accessToken = authorizedClient.getAccessToken();
//
//                String email = authToken.getPrincipal().getAttribute("email");
//                Optional<User> optionalUser = userRepository.findByEmail(email);
//
//                optionalUser.ifPresent(user -> {
//                    user.setGoogleAccessToken(accessToken.getTokenValue());
//                    user.setGoogleAccessTokenExpiry(accessToken.getExpiresAt());
//
//                    // También puedes acceder al refresh token si está disponible
//                    var refreshToken = authorizedClient.getRefreshToken();
//                    if (refreshToken != null) {
//                        user.setGoogleRefreshToken(refreshToken.getTokenValue());
//                    }
//
//                    userRepository.save(user);
//                });
//
//                return ResponseEntity.ok("Tokens guardados en el usuario correctamente");
//            }
//        }
//        return ResponseEntity.status(401).body("No se pudo obtener el token de acceso");
//    }
//
//}
