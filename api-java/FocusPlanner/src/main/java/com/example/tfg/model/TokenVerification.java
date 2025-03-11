package com.example.tfg.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "token_verification")
public class TokenVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "verification_token", nullable = false)
    private String verificationToken;

    @Column(name = "expiration_date", nullable = false)
    private LocalDateTime expiryDate;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    public TokenVerification(String token, User user) {
        this.verificationToken = token;
        this.user = user;
        this.expiryDate = LocalDateTime.now().plusHours(24); // Expira en 24 horas
    }
}
