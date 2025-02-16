package com.example.tfg.Auth;

import com.example.tfg.model.User;
import com.example.tfg.service.TokenVerificationService;
import com.example.tfg.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Autowired
    private TokenVerificationService tokenVerificationService;

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request){
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody User user) {
        try {
            // Asegúrate de pasar todos los argumentos que requiere el método registerUser
            userService.registerUser(
                    user.getEmail(),      // email
                    user.getPassword(),   // password
                    user.getUsername(),   // username
                    user.getFirstname(),  // firstname
                    user.getLastname(),   // lastname
                    user.getCountry()     // country
            );
            return ResponseEntity.ok("¡Te has registrado con éxito! Revisa tu correo para verificar tu cuenta.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Hubo un error durante el registro.");
        }
    }

    @GetMapping("/verify")
    public ResponseEntity<String> verifyAccount(@RequestParam("token") String token) {
        boolean isVerified = tokenVerificationService.verifyToken(token);

        if (isVerified) {
            return ResponseEntity.ok("Tu cuenta ha sido verificada con éxito.");
        } else {
            return ResponseEntity.status(400).body("Token de verificación inválido o expirado.");
        }
    }

}
