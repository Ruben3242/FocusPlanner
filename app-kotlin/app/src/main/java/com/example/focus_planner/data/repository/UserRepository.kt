package com.example.focus_planner.data.repository

import android.content.Context
import com.example.focus_planner.data.local.AppDatabase
import com.example.focus_planner.data.local.UserTokenDao
import com.example.focus_planner.model.LoginResponse
import com.example.focus_planner.model.RegisterResponse
import com.example.focus_planner.network.ApiService
import com.example.focus_planner.network.LoginRequest
import com.example.focus_planner.network.RegisterRequest
import com.example.focus_planner.network.RetrofitInstance
import retrofit2.Response

class UserRepository {
    class UserRepository(private val apiService: ApiService = RetrofitInstance.api) {
        suspend fun login(email: String, password: String) = apiService.login(LoginRequest(email, password))
        suspend fun register(name: String, email: String, password: String) = apiService.register(RegisterRequest(name, email, password))
    }
}
