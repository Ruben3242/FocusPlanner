package com.example.tfg.service;

import com.example.tfg.GogleCalendar.Auth.Permisos.GoogleCalendarService;
import com.example.tfg.model.Task;
import com.example.tfg.security.model.AuthResponse;
import com.example.tfg.security.model.LoginRequest;
import com.example.tfg.security.Jwt.JwtService;
import com.example.tfg.enums.TaskStatus;
import com.example.tfg.security.model.TokenVerification;
import com.example.tfg.enums.Role;
import com.example.tfg.model.User;
import com.example.tfg.repository.TaskRepository;
import com.example.tfg.security.repository.TokenVerificationRepository;
import com.example.tfg.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final TokenVerificationRepository tokenVerificationRepository;
    private final TaskRepository taskRepository;
    @Autowired
    private final GoogleCalendarService googleCalendarService;


    public User registerUser(String email, String password, String username, String firstname, String lastname, String country) {
        // Verificar si el correo ya existe
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email is already taken.");
        }

        // Crear y guardar el nuevo usuario
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setUsername(username);
        user.setFirstname(firstname);
        user.setLastname(lastname);
        user.setCountry(country);
        user.setRemoveCompletedExpiredTasks(false);
        user.setRole(Role.ADMIN);
        user.setVerified(false);

        // Guardar el usuario en la base de datos
        userRepository.save(user);

        // Generar el token de verificación
        String token = UUID.randomUUID().toString();
        TokenVerification verificationToken = new TokenVerification(token, user);
        tokenVerificationRepository.save(verificationToken);

        // Enviar email con el token
        emailService.sendVerificationEmail(user, token);

        return user;
    }

    // Método para login
    public AuthResponse login(LoginRequest request) {
        // Verificar si el usuario existe
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow(() -> new RuntimeException("User not found"));

        // Verificar si el usuario está verificado
        if (!user.isVerified()) {
            throw new RuntimeException("User is not verified.");
        }

        // Autenticar las credenciales
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Generar el token
        String token = jwtService.getToken(user);

        return AuthResponse.builder().token(token).message("Login successful").build();
    }

    // Método para verificar la cuenta del usuario
    public String verifyUser(String token) {
        // Buscar en la tabla de tokens de verificación
        TokenVerification tokenVerification = tokenVerificationRepository.findByVerificationToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token or expired"));

        User user = tokenVerification.getUser();

        if (user.isVerified()) {
            throw new RuntimeException("User is already verified.");
        }

        // Marcar usuario como verificado
        user.setVerified(true);
        userRepository.save(user);

        // Eliminar el token después de la verificación
        tokenVerificationRepository.delete(tokenVerification);

        return "User verified successfully!";
    }

//    @Transactional
//    public void deleteCompletedExpiredTasks(User user) {
//        // Eliminar tareas completadas o vencidas para el usuario
//        taskRepository.deleteByUserAndStatusIn(user, List.of(TaskStatus.COMPLETED/*, TaskStatus.EXPIRED*/));
//    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
    }
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User registerOrUpdateUser(String email, String firstName, String lastName) {
        User user = userRepository.findByEmail(email).orElse(new User());
        user.setEmail(email);
        user.setFirstname(firstName);
        user.setLastname(lastName);

        // Si el usuario ya existe, lo actualizamos, sino lo creamos.
        return userRepository.save(user);
    }

    @Transactional
    public void deleteUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));

        List<Task> tasks = taskRepository.findAllByUserId(userId);

        for (Task task : tasks) {
            try {
                if (user.getGoogleAccessToken() != null && task.getGoogleCalendarEventId() != null) {
                    googleCalendarService.deleteCalendarEvent(task.getGoogleCalendarEventId(), user);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        taskRepository.deleteAll(tasks);
        tokenVerificationRepository.deleteAllByUserId(userId);
        userRepository.delete(user);
    }


    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with ID: " + id));
    }
}