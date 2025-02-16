//package com.example.tfg.User;
//
//import jakarta.mail.internet.MimeMessage;
//import lombok.RequiredArgsConstructor;
//import org.springframework.mail.javamail.JavaMailSender;
//import org.springframework.mail.javamail.MimeMessageHelper;
//import org.springframework.stereotype.Service;
//
//@Service
//@RequiredArgsConstructor
//public class TestEmailService {
//    private final JavaMailSender mailSender;
//
//    public void sendTestEmail() {
//        try {
//            MimeMessage message = mailSender.createMimeMessage();
//            MimeMessageHelper helper = new MimeMessageHelper(message, true);
//
//            helper.setTo("rloruben555@gmail.com");
//            helper.setSubject("Test de Correo SMTP");
//            helper.setText("Este es un correo de prueba desde Spring Boot con Gmail SMTP.");
//
//            mailSender.send(message);
//            System.out.println("Correo enviado con éxito!");
//        } catch (Exception e) {
//            e.printStackTrace();
//            System.out.println("Error al enviar el correo: " + e.getMessage());
//        }
//    }
//}
