package com.example.tfg.security.repository;

import com.example.tfg.security.model.TokenVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TokenVerificationRepository extends JpaRepository<TokenVerification, Long> {
    Optional<TokenVerification> findByVerificationToken(String token);

    Optional<TokenVerification> findByUserId(Long userId);

    void deleteByUserId(Long userId);

    void deleteAllByUserId(Long userId);
}
