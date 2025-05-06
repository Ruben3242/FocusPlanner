package com.example.tfg.security.controller;

import com.example.tfg.security.model.*;
import com.example.tfg.security.service.AuthService;
import com.example.tfg.security.Jwt.JwtService;
import com.example.tfg.model.User;
import com.example.tfg.repository.UserRepository;
import com.example.tfg.security.service.RefreshTokenService;
import com.example.tfg.security.service.TokenVerificationService;
import com.example.tfg.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Autowired
    private TokenVerificationService tokenVerificationService;

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    @ResponseBody
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request){
        return ResponseEntity.ok(authService.login(request));
    }
    @PostMapping("/refresh")
    @ResponseBody
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody RefreshTokenRequest request) {
        return refreshTokenService.findByToken(request.getRefreshToken())
                .filter(refreshTokenService::isTokenValid)
                .map(refreshToken -> {
                    String accessToken = jwtService.getToken(refreshToken.getUser());
                    return ResponseEntity.ok(AuthResponse.builder()
                            .token(accessToken)
                            .refreshToken(refreshToken.getToken())
                            .build());
                }).orElseGet(() -> ResponseEntity.badRequest().build());
    }
    @PostMapping("/refresh-token")
    @ResponseBody
    public AuthResponse refreshAccessToken(@RequestBody String refreshToken) {
        System.out.println("Received refresh token: " + refreshToken);  // Agrega este log

        RefreshToken refreshTokenEntity = refreshTokenService.findByToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        if (!refreshTokenService.isTokenValid(refreshTokenEntity)) {
            throw new RuntimeException("Refresh token has expired");
        }

        User user = refreshTokenEntity.getUser();
        String newAccessToken = jwtService.getToken(user);

        return AuthResponse.builder()
                .token(newAccessToken)
                .refreshToken(refreshToken) // Devuelve el mismo refresh token si sigue siendo válido
                .message("Access token refreshed successfully!")
                .build();
    }



    @PostMapping("/register")
    @ResponseBody
    public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest user) {
        try {
            authService.register(user);
            RegisterResponse response = new RegisterResponse("¡Te has registrado con éxito! Revisa tu correo para verificar tu cuenta.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new RegisterResponse("Hubo un error durante el registro."));
        }
    }

    @GetMapping("/verify")
    public String verifyAccount(@RequestParam("token") String token) {
        boolean isVerified = tokenVerificationService.verifyToken(token);
        return isVerified ? "verification_success" : "verification_error";
    }
}
