package com.example.tfg.GogleCalendar.Auth.Permisos;

import com.example.tfg.model.User;
import com.example.tfg.repository.UserRepository;
import com.example.tfg.security.Jwt.JwtService;
import com.example.tfg.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
    private final UserService userService;
    private final UserRepository userRepository;

    @Autowired
    private OAuth2AuthorizedClientService authorizedClientService;

    public OAuth2LoginSuccessHandler(JwtService jwtService, UserService userService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2AuthenticationToken authToken = (OAuth2AuthenticationToken) authentication;
        OAuth2User oauthUser = authToken.getPrincipal();

        // Obtener el email del usuario autenticado
        String email = (String) oauthUser.getAttributes().get("email");

        // Obtener el usuario desde la base de datos
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con email: " + email));

        // Generar tu JWT
        String jwt = jwtService.getToken(user);

        // Obtener el access_token de Google para usar la API de Calendar
        OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(
                authToken.getAuthorizedClientRegistrationId(),
                authToken.getName());

        String googleAccessToken = authorizedClient.getAccessToken().getTokenValue();
        String googleRefreshToken = authorizedClient.getRefreshToken() != null
                ? authorizedClient.getRefreshToken().getTokenValue()
                : null;
        Instant googleAccessTokenExpiry = authorizedClient.getAccessToken().getExpiresAt();

        System.out.println("Google Access Token: " + googleAccessToken);
        System.out.println("Google Refresh Token: " + googleRefreshToken);
        System.out.println("Google Access Token Expiry: " + googleAccessTokenExpiry);
        user.setGoogleAccessToken(googleAccessToken);
        user.setGoogleAccessTokenExpiry(googleAccessTokenExpiry);
        user.setGoogleRefreshToken(googleRefreshToken);
         userRepository.save(user);

        // Redirigir al frontend con ambos tokens (ten cuidado con exponer el token de Google)
        String redirectUrl = String.format(
                "https://0ae5-92-189-98-92.ngrok-free.app/oauth2/success.html?token=%s&googleToken=%s",
                jwt, googleAccessToken);

        redirectStrategy.sendRedirect(request, response, redirectUrl);
    }
}
