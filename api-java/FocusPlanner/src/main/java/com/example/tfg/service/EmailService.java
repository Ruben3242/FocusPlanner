package com.example.tfg.service;

import com.example.tfg.model.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Async
    public void sendVerificationEmail(User user, String token) {
        System.out.println("Enviando correo de verificación a: " + user.getEmail());

        String subject = "Verificación de Cuenta";
        String body = "<html>"
                + "<body style=\"font-family: Arial, sans-serif; background-color: #f4f7fc; padding: 20px; text-align: center;\">"
                + "<div style=\"max-width: 600px; margin: auto; background-color: #ffffff; padding: 20px; border-radius: 10px; box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);\">"
                + "<h2 style=\"color: #4CAF50;\">¡Bienvenido, " + user.getUsername() + "!</h2>"
                + "<p style=\"font-size: 16px; color: #333;\">Gracias por registrarte en nuestra plataforma. Para activar tu cuenta, por favor haz clic en el botón de abajo.</p>"
                + "<a href=\"http://localhost:8080/api/auth/verify?token=" + token + "\" "
                + "style=\"display: inline-block; background-color: #4CAF50; color: white; padding: 14px 24px; font-size: 16px; text-decoration: none; border-radius: 6px; margin: 20px 0;\">"
                + "Verificar mi cuenta</a>"
                + "<p style=\"font-size: 14px; color: #777;\">Este enlace caducará en 24 horas.</p>"
                + "<hr style=\"border: none; height: 1px; background-color: #ddd; margin: 20px 0;\">"
                + "<p style=\"font-size: 12px; color: #999;\">Si no solicitaste este registro, puedes ignorar este mensaje.</p>"
                + "</div>"
                + "</body>"
                + "</html>";

        sendEmail(user.getEmail(), subject, body);
    }

    public void sendEmail(String to, String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true); // Asegurar que se envía como HTML
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Error al enviar el correo", e);
        }
    }

}