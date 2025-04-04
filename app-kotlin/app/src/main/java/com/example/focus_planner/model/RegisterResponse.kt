package com.example.focus_planner.model

data class RegisterResponse(
    val message: String
)
data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)