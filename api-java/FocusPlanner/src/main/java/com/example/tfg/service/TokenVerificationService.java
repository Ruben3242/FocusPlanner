package com.example.tfg.service;


import com.example.tfg.Jwt.TokenVerification;
import com.example.tfg.model.User;
import com.example.tfg.repository.TokenVerificationRepository;
import com.example.tfg.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TokenVerificationService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenVerificationRepository tokenVerificationRepository;

    public boolean verifyToken(String token) {
        TokenVerification tokenVerification = tokenVerificationRepository.findByToken(token);

        if (tokenVerification != null) {
            User user = tokenVerification.getUser();
            user.setVerified(true); // Marcar como verificado
            userRepository.save(user);

            // Eliminar el token después de su uso
            tokenVerificationRepository.delete(tokenVerification);

            return true;
        }
        return false;
    }
}