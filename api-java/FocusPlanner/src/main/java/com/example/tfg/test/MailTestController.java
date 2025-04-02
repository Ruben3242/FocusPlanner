package com.example.tfg.test;

import com.example.tfg.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class MailTestController {
    private final EmailService emailService;

    @GetMapping("/send-test-email")
    public ResponseEntity<String> sendTestEmail() {
        try {
            emailService.sendEmail("rloruben555@gmail.com", "Prueba", "Este es un correo de prueba");
            return ResponseEntity.ok("Correo enviado con éxito.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al enviar correo: " + e.getMessage());
        }
    }
}

