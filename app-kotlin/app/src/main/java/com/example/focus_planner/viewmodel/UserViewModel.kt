package com.example.focus_planner.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.focus_planner.network.ApiService
import com.example.focus_planner.network.LoginRequest
import com.example.focus_planner.network.RegisterRequest
import com.example.focus_planner.network.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Response

class UserViewModel(private val apiService: ApiService) : ViewModel() {

    fun login(email: String, password: String, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val loginRequest = LoginRequest(email, password)
                val response = apiService.login(loginRequest)
                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    if (loginResponse != null) {
                        // Manejar la respuesta (guardar token, etc.)
                        Log.d("Login", "Token: ${loginResponse.token}")
                        callback(true)
                    } else {
                        callback(false)
                    }
                } else {
                    callback(false)
                }
            } catch (e: Exception) {
                Log.e("Login", "Error en login: ${e.message}")
                callback(false)
            }
        }
    }

    fun register(name: String, email: String, password: String, onResult: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val response = apiService.register(RegisterRequest(name, email, password))
                if (response.isSuccessful) {
                    onResult("Registro exitoso, ya puedes verificar tu cuenta en tu correo electronico .")
                } else {
                    onResult("Error en el registro, intentalo nuevamente.")
                }
            } catch (e: Exception) {
                onResult("Error en el registro, por favor intenta de nuevo.")
            }
        }
    }
}
