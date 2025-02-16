package com.example.tfg.Auth;

import com.example.tfg.Jwt.JwtService;
import com.example.tfg.User.Role;
import com.example.tfg.model.User;
import com.example.tfg.repository.UserRepository;
import com.example.tfg.service.EmailService;
import com.example.tfg.service.UserService;  // Asegúrate de importar UserService
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final UserService userService;  // Inyectamos UserService

    // Método para login
    public AuthResponse login(LoginRequest request) {
        // Verificar si el usuario existe
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Verificar si el usuario está verificado
        if (!user.isVerified()) {
            throw new RuntimeException("User is not verified.");
        }

        // Autenticar las credenciales
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Generar el token JWT
        String token = jwtService.getToken(user);

        // Devolver la respuesta con el token y el mensaje
        return AuthResponse.builder()
                .token(token)
                .message("Login successful!")  // Aquí asignamos un mensaje explícito
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

        // Generar un token de verificación y enviarlo por correo
        String verificationToken = UUID.randomUUID().toString();
        user.setVerificationToken(verificationToken);
        System.out.println("Token generado: " + verificationToken);
        userRepository.save(user);

        emailService.sendVerificationEmail(user, verificationToken);

        // Devolver una respuesta con el mensaje
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
