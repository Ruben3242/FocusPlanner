package com.example.tfg.GogleCalendar.Auth;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/oauth2")
public class GoogleAuthController {

    private final OAuth2AuthorizedClientService authorizedClientService;

    public GoogleAuthController(OAuth2AuthorizedClientService authorizedClientService) {
        this.authorizedClientService = authorizedClientService;
    }

    @GetMapping("/callback")
    public ResponseEntity<String> oauth2Callback(@RequestParam(value = "code", required = false) String code,
                                                 Authentication authentication) {
        if (authentication instanceof OAuth2AuthenticationToken authToken) {
            OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(
                    authToken.getAuthorizedClientRegistrationId(), authToken.getName());

            if (authorizedClient != null) {
                OAuth2AccessToken accessToken = authorizedClient.getAccessToken();
                return ResponseEntity.ok("Token de acceso: " + accessToken.getTokenValue());
            }
        }
        return ResponseEntity.status(401).body("No se pudo obtener el token de acceso");
    }
}
