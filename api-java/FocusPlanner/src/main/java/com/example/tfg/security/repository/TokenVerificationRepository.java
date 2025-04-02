package com.example.tfg.security.repository;

import com.example.tfg.security.model.TokenVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

//public interface TokenVerificationRepository extends JpaRepository<TokenVerification, String> {
//    TokenVerification findByToken(String token);
//}
//@Repository
//public interface TokenVerificationRepository extends JpaRepository<TokenVerification, Long> {
//    TokenVerification findByToken(String token);
//
//    Optional<TokenVerification> findByUserId(Long userId);  // Busca por userId
//    void deleteByUserId(Long userId);  // Elimina por userId
//}

@Repository
public interface TokenVerificationRepository extends JpaRepository<TokenVerification, Long> {
    Optional<TokenVerification> findByVerificationToken(String token);

    Optional<TokenVerification> findByUserId(Long userId);

    void deleteByUserId(Long userId);
}
