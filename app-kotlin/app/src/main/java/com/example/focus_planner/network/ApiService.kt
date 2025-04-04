package com.example.focus_planner.network

import com.example.focus_planner.model.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

// Definimos las solicitudes HTTP para login y registro
data class LoginRequest(val email: String, val password: String)
data class RegisterRequest(val name: String, val email: String, val password: String)

interface ApiService {

    @POST("api/auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<String>
}
