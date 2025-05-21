package com.example.tfg.security.service;

import com.example.tfg.security.model.LoginRequest;
import com.example.tfg.security.model.RegisterRequest;
import com.example.tfg.security.model.AuthResponse;
import com.example.tfg.security.Jwt.JwtService;
import com.example.tfg.security.model.RefreshToken;
import com.example.tfg.model.User;
import com.example.tfg.repository.UserRepository;
import com.example.tfg.service.EmailService;
import com.example.tfg.service.UserService;  // Asegúrate de importar UserService
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    private final JwtService jwtService;
    private final JavaMailSender mailSender;
    private final AuthenticationManager authenticationManager;

    private final EmailService emailService;
    private final UserService userService;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;


    public AuthResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    System.out.println("[LOGIN] Usuario no encontrado con email: " + request.getEmail());
                    return new RuntimeException("User not found");
                });

        System.out.println("[LOGIN] Intentando login para email: " + request.getEmail());
        System.out.println("[DEBUG] Contraseña introducida: " + request.getPassword());
        System.out.println("[DEBUG] Hash en base de datos: " + user.getPassword());
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hash = encoder.encode("123");
        System.out.println("Hash de '123': " + hash);
        System.out.println("[DEBUG] Coinciden?: " + passwordEncoder.matches(request.getPassword(), user.getPassword()));


        if (!user.isVerified()) {
            System.out.println("[LOGIN] Usuario no verificado: " + user.getEmail());
            throw new RuntimeException("User is not verified.");
        }

        try {
            System.out.println("[LOGIN] Autenticando usuario con AuthenticationManager...");
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            System.out.println("[LOGIN] Autenticación exitosa.");
        } catch (Exception e) {
            System.out.println("[LOGIN] Falló la autenticación: " + e.getMessage());
            throw new RuntimeException("Credenciales inválidas.");
        }

        String accessToken = jwtService.getToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        System.out.println("[LOGIN] Token generado correctamente.");
        System.out.println("[LOGIN] Login completado para usuario: " + user.getEmail());

        return AuthResponse.builder()
                .token(accessToken)
                .refreshToken(refreshToken.getToken())
                .message("Login successful!")
                .build();
    }



    // Método para registrar un nuevo usuario
    public AuthResponse register(RegisterRequest request) {
        User user = userService.registerUser(
                request.getEmail(),
                request.getPassword(),
                request.getUsername(),
                request.getFirstname(),
                request.getLastname(),
                request.getCountry()
        );

        return AuthResponse.builder()
                .message("User registered successfully. Please verify your email.")
                .build();
    }


    // Método para verificar el token de un usuario
    public String verifyUser(String token) {
        // Buscar el usuario con el token de verificación
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token or user not found"));

        // Verificar que el token no haya sido usado previamente
        if (user.isVerified()) {
            throw new RuntimeException("User is already verified.");
        }

        // Cambiar el estado de verificación del usuario
        user.setVerified(true);
        user.setVerificationToken(null); // Limpiar el token después de la verificación
        userRepository.save(user);

        return "User verified successfully!";
    }
//    public void sendVerificationEmail(User user) {
//        System.out.println("Enviando correo de verificación a: " + user.getEmail());
//
//        String subject = "Verificación de Cuenta";
//        String body = "Por favor, haz clic en el siguiente enlace para verificar tu cuenta: "
//                + "http://localhost:8080/api/auth/verify?token=" + user.getVerificationToken();
//
//        sendEmail(user.getEmail(), subject, body);
//    }
//
//    public void sendEmail(String to, String subject, String body) {
//        try {
//            MimeMessage message = mailSender.createMimeMessage();
//            MimeMessageHelper helper = new MimeMessageHelper(message, true);
//            helper.setTo(to);
//            helper.setSubject(subject);
//            helper.setText(body, true); // true para habilitar HTML en el correo
//
//            mailSender.send(message);
//        } catch (MessagingException e) {
//            throw new RuntimeException("Error al enviar el correo", e);
//        }
//    }


}
