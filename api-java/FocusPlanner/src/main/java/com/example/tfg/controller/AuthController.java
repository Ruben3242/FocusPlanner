package com.example.tfg.controller;

import com.example.tfg.model.User;
import com.example.tfg.security.JwtResponse;
import com.example.tfg.security.JwtTokenProvider;
import com.example.tfg.security.LoginRequest;
import com.example.tfg.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private final AuthenticationManager authenticationManager;

    @Autowired
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    private final CustomUserDetailsService userDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public AuthController(AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider, CustomUserDetailsService userDetailsService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        // Verifica si ya existe un usuario con el correo proporcionado
        if (userDetailsService.existsByEmail(user.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("User with this email already exists");
        }

        // Codifica la contraseña y marca el usuario como habilitado
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setVerified(true); // Asegúrate de que el usuario esté habilitado al registrarse
        userDetailsService.save(user);

        // Responde con un código de estado 201 Created
        return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully");
    }


    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        try {
            // Log para verificar el email y la contraseña proporcionados
            System.out.println("Attempting login with email: " + loginRequest.getEmail());

            // Autenticar las credenciales del usuario
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

            // Establecer el contexto de seguridad
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Generar el token JWT
            String jwt = jwtTokenProvider.generateToken(authentication);

            // Log para verificar que el token se generó correctamente
            System.out.println("Generated JWT token: " + jwt);

            // Devuelve el token en la respuesta
            return ResponseEntity.ok(new JwtResponse(jwt));
        } catch (Exception e) {
            // Log para verificar el error en caso de autenticación fallida
            System.err.println("Authentication failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
    }

}
