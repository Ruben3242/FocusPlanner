package com.example.focus_planner.model

data class LoginResponse(
    val token: String,
    val message: String,
    val refreshToken: String
)
data class LoginRequest(
    val email: String,
    val password: String
)