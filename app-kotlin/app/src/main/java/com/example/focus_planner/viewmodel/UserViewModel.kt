package com.example.focus_planner.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class UserViewModel : ViewModel() {

    fun login(email: String, password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            delay(2000) // Simulamos una llamada a la API
            onResult(email == "test@example.com" && password == "password123")
        }
    }

    fun register(name: String, email: String, password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            delay(2000) // Simulamos una llamada a la API
            onResult(email.contains("@") && password.length >= 6)
        }
    }
}
