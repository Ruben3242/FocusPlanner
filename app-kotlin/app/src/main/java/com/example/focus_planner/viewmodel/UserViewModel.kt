package com.example.focus_planner.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.example.focus_planner.data.repository.UserRepository

class UserViewModel : ViewModel() {
    private val userRepository = UserRepository()

    var isLoggedIn = mutableStateOf(false)

    // Función para manejar el login
    fun login(email: String, password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                // Realizar la autenticación (suponiendo una función en el repositorio)
                val token = userRepository.authenticateUser(email, password)
                if (token != null) {
                    // Guardar el token de JWT (puedes usar SharedPreferences aquí)
                    isLoggedIn.value = true
                    onResult(true)
                } else {
                    onResult(false)
                }
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }
}
