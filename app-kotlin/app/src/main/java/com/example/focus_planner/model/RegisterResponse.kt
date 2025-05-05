package com.example.focus_planner.model

data class RegisterResponse(
    val message: String
)
data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val firstname: String? = null,
    val lastname: String? = null,
    val country: String? = null
)
