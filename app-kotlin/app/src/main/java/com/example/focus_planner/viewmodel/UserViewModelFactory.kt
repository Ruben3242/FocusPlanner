package com.example.focus_planner.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.focus_planner.data.repository.UserRepository
import com.example.focus_planner.network.ApiService
import com.example.focus_planner.network.RetrofitInstance

class UserViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
            val userRepository = UserRepository(context) // Instancia según tu implementación
            val apiService = RetrofitInstance.getRetrofitInstance().create(ApiService::class.java)
            return UserViewModel(userRepository, apiService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
