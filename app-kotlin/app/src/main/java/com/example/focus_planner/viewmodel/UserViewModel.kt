package com.example.focus_planner.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.focus_planner.data.local.AppDatabase
import com.example.focus_planner.data.local.UserToken
import com.example.focus_planner.data.local.UserTokenDao
import com.example.focus_planner.data.repository.UserRepository
import com.example.focus_planner.network.ApiService
import com.example.focus_planner.network.LoginRequest
import com.example.focus_planner.network.RegisterRequest
import com.example.focus_planner.network.RetrofitInstance
import com.example.focus_planner.utils.SharedPreferencesManager
import dagger.hilt.android.internal.Contexts.getApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Response


class UserViewModel(private val context: Context,private val userRepository: UserRepository) : ViewModel() {
    private val apiService: ApiService = RetrofitInstance.getRetrofitInstance().create(ApiService::class.java)
    private val userTokenDao: UserTokenDao = AppDatabase.getDatabase(context).userTokenDao()
    fun getUserTokenDao(): UserTokenDao {
        return userTokenDao
    }

    fun login(email: String, password: String, context: Context, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val response = apiService.login(LoginRequest(email, password))
                if (response.isSuccessful) {
                    val token = response.body()?.token
                    if (token != null) {
                        val expirationDate = System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000
                        SharedPreferencesManager.saveToken(context, token, expirationDate)
                        saveToken(email, token, expirationDate)
                        Log.d("UserViewModel", "Login exitoso, token guardado: $token")
                        onResult(true)
                    } else {
                        Log.d("UserViewModel", "Token nulo después de login")
                        onResult(false)
                    }
                } else {
                    onResult(false)
                }
            } catch (e: Exception) {
                Log.e("UserViewModel", "Error en login: ${e.message}")
                onResult(false)
            }
        }
    }


    // Función para hacer logout
    fun logout(context: Context) {
        SharedPreferencesManager.clearToken(context)
        viewModelScope.launch(Dispatchers.IO) {
            userTokenDao.deleteTokenByEmail(SharedPreferencesManager.getToken(context) ?: "")
        }
    }

    // Función para registrar un nuevo usuario
    fun register(name: String, email: String, password: String, onResult: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val response = apiService.register(RegisterRequest(name, email, password))
                if (response.isSuccessful) {
                    onResult("Registro exitoso, ya puedes verificar tu cuenta en tu correo electronico.")
                } else {
                    onResult("Error en el registro, intentalo nuevamente.")
                }
            } catch (e: Exception) {
                onResult("Error en el registro, por favor intenta de nuevo.")
            }
        }
    }

    // Función para guardar el token en la base de datos
    private fun saveToken(email: String, token: String, expiryTime: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val userToken = UserToken(email, token, expiryTime)
            userTokenDao.insert(userToken)
        }
    }

    // Función para obtener el token almacenado en la base de datos
    suspend fun getToken(email: String): UserToken? {
        return userTokenDao.getTokenByEmail(email)
    }

    // Función para eliminar el token de la base de datos (logout)
    fun removeToken(email: String) {
        viewModelScope.launch {
            userTokenDao.deleteTokenByEmail(email)
        }
    }
}
