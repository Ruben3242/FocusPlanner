package com.example.tfg.repository;

import com.example.tfg.Jwt.TokenVerification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TokenVerificationRepository extends JpaRepository<TokenVerification, String> {
    TokenVerification findByToken(String token);
}
