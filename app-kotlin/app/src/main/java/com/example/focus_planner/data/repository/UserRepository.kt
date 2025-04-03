package com.example.focus_planner.data.repository

class UserRepository {

    // Simulamos la autenticación para este ejemplo
    suspend fun authenticateUser(email: String, password: String): String? {
        return if (email == "user@example.com" && password == "password123") {
            "jwt-token-example" // Devuelve el token si la autenticación es exitosa
        } else {
            null // Si el login falla, devuelve null
        }
    }
}
