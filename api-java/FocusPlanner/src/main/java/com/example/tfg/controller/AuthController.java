package com.example.tfg.controller;

import com.example.tfg.model.User;
import com.example.tfg.security.JwtResponse;
import com.example.tfg.security.JwtTokenProvider;
import com.example.tfg.security.JwtUtil;
import com.example.tfg.security.LoginRequest;
import com.example.tfg.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
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
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public AuthController(AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider, CustomUserDetailsService userDetailsService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        if (userDetailsService.existsByEmail(user.getEmail())) {
            // Si ya existe, devuelve un conflicto 409
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("User with this email already exists");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword())); // Codifica la contraseña
        userDetailsService.save(user); // Actualiza el usuario
        return ResponseEntity.status(HttpStatus.CONFLICT).body( "User registered successfully");
    }

    /*@PostMapping("/login")
    public String login(@RequestBody User user) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword())
            );
            return jwtUtil.generateToken(user.getEmail()); // Devuelve el token
        } catch (AuthenticationException e) {
            throw new RuntimeException("Invalid credentials");
        }
    }
     */
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        try {
            // Autenticar las credenciales del usuario
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Generar el token JWT
            String jwt = jwtTokenProvider.generateToken(authentication);
            return ResponseEntity.ok(new JwtResponse(jwt));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
    }
}
