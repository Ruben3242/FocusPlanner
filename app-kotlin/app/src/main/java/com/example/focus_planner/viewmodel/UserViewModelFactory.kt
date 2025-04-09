package com.example.focus_planner.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.focus_planner.data.repository.UserRepository

class UserViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
            val userRepository = UserRepository() // Instancia según tu implementación
            return UserViewModel(context, userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
