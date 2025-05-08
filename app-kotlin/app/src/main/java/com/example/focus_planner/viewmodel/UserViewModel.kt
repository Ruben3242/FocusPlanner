package com.example.focus_planner.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.focus_planner.data.model.User
import com.example.focus_planner.data.model.UserToken
import com.example.focus_planner.data.repository.UserRepository
import com.example.focus_planner.model.LoginRequest
import com.example.focus_planner.model.RegisterRequest
import com.example.focus_planner.network.ApiService
import com.example.focus_planner.network.RetrofitInstance
import com.example.focus_planner.utils.SharedPreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val apiService: ApiService

) : ViewModel() {
//    private val apiService: ApiService = RetrofitInstance.getRetrofitInstance().create(ApiService::class.java)
//    private val userTokenDao: UserTokenDao = AppDatabase.getDatabase(context).userTokenDao()
//    fun getUserTokenDao(): UserTokenDao {
//        return userTokenDao
//    }

    // Función para comprobar si el token necesita ser refrescado
    private fun refreshAuthToken(context: Context, onResult: (Boolean) -> Unit) {
        val refreshToken = SharedPreferencesManager.getRefreshToken(context)
        if (refreshToken != null) {
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val refreshToken = SharedPreferencesManager.getRefreshToken(context) ?: return@launch
                    val mediaType = "text/plain".toMediaType()
                    val requestBody = refreshToken.toRequestBody(mediaType)

                    val response = RetrofitInstance.api.refreshToken(requestBody)

                    if (response.isSuccessful) {
                        val newToken = response.body()?.token
                        val newRefreshToken = response.body()?.refreshToken
                        val expirationDate = System.currentTimeMillis() + 24 * 60 * 60 * 1000 // Nuevo token con expiración de 1 día

                        if (newToken != null && newRefreshToken != null) {
                            // Guarda el nuevo token, refreshToken y la fecha de expiración
                              SharedPreferencesManager.saveLoginData(context, newToken, newRefreshToken, expirationDate)
//                            SharedPreferencesManager.saveLoginData(
//                                context,
//                                newToken,
//                                refreshToken,
//                                System.currentTimeMillis() - (8 * 24 * 60 * 60 * 1000) // hace 2 días
//                            )
                            Log.d("UserViewModel", "Token refrescado exitosamente.")
                            onResult(true)
                        } else {
                            Log.d("UserViewModel", "Error al obtener el nuevo token.")
                            onResult(false)
                        }
                    } else {
                        Log.d("UserViewModel", "Error al refrescar el token.")
                        onResult(false)
                    }
                } catch (e: Exception) {
                    Log.e("UserViewModel", "Error en refreshToken: ${e.message}")
                    onResult(false)
                }
            }
        } else {
            Log.d("UserViewModel", "No se encontró el refresh token.")
            onResult(false)
        }
    }

    // Función para manejar login
    fun login(email: String, password: String, context: Context, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val response = apiService.login(LoginRequest(email, password))
                if (response.isSuccessful) {
                    val token = response.body()?.token
                    val refreshToken = response.body()?.refreshToken
                    val expirationDate = System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000 // Expiración en 7 días

                    // Log para verificar los valores obtenidos del login
                    Log.d("UserViewModel", "Login exitoso: token=$token, refreshToken=$refreshToken")

                    if (token != null && refreshToken != null) {
                        // Guarda el token, refreshToken y la fecha de expiración
                        SharedPreferencesManager.saveLoginData(context, token, refreshToken, expirationDate)
                        Log.d("UserViewModel", "Login exitoso, token guardado.")
                        val profileResponse = apiService.getUserProfile("Bearer $token")
                        if (profileResponse.isSuccessful) {
                            val user = profileResponse.body()
                            if (user != null) {
                                SharedPreferencesManager.saveUserId(context, user.id)
                                Log.d("UserViewModel", "ID de usuario guardado: ${user.id}")
                            } else {
                                Log.e("UserViewModel", "Usuario nulo al obtener perfil")
                            }
                        } else {
                            Log.e("UserViewModel", "Error al obtener perfil: ${profileResponse.code()}")
                        }
                        onResult(true)
                    } else {
                        Log.d("UserViewModel", "Token o refreshToken nulos.")
                        onResult(false)
                    }
                } else {
                    Log.d("UserViewModel", "Error en el login: ${response.code()}")
                    onResult(false)
                }
            } catch (e: Exception) {
                Log.e("UserViewModel", "Error en login: ${e.message}")
                onResult(false)
            }
        }
    }
    // Función para comprobar el estado del token antes de hacer una solicitud
    fun checkTokenAndRefresh(context: Context, onResult: (Boolean) -> Unit) {
        if (SharedPreferencesManager.isTokenExpired(context) || SharedPreferencesManager.isSessionExpired(context)) {
            // Si el token ha caducado o la sesión ha caducado, refrescar el token
            refreshAuthToken(context, onResult)
        } else {
            // Si el token es válido, continuar con la solicitud
            Log.d("UserViewModel", "Token válido, continúa con la solicitud.")
            onResult(true)
        }
    }

    // Función para hacer logout
    fun logout(context: Context) {
        SharedPreferencesManager.clearToken(context)
        val email = SharedPreferencesManager.getToken(context) ?: return
        viewModelScope.launch(Dispatchers.IO) {
            userRepository.deleteTokenByEmail(email)
        }
    }

    // Función para registrar un nuevo usuario
    fun register(username: String, email: String, password: String, onResult: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val request = RegisterRequest(username, email, password, "", "", "")
                val response = apiService.register(request)

                Log.d("Register", "Código de respuesta: ${response.code()}")
                Log.d("Register", "Cuerpo: ${response.body()?.message}")
                Log.d("Register", "ErrorBody: ${response.errorBody()?.string()}")

                if (response.isSuccessful) {
                    val message = response.body()?.message ?: "Registro exitoso."
                    onResult(message)
                } else {
                    val error = response.errorBody()?.string()
                    onResult("Error en el registro: ${error ?: "intenta nuevamente"}")
                }
            } catch (e: Exception) {
                Log.e("Register", "Excepción durante el registro", e)
                onResult("Error en el registro, por favor intenta de nuevo.")
            }
        }
    }



    // Función para guardar el token en la base de datos
    private fun saveToken(email: String, token: String, expiryTime: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val newToken = UserToken(
                email = email,
                token = token,
                expiryTime = expiryTime,
                username = null,
                firstName = null,
                lastName = null,
                isVerified = true
            )

            userRepository.insertToken(newToken)
        }
    }

    // Función para obtener el token almacenado en la base de datos
    suspend fun getToken(email: String) {
        userRepository.deleteTokenByEmail(email)
    }

    // Función para eliminar el token de la base de datos (logout)
    fun removeToken(email: String) {
        viewModelScope.launch {
            userRepository.deleteTokenByEmail(email)
        }
    }
    // Función para obtener el usuario actual desde la base de datos local
    fun getLocalUser(context: Context, onResult: (UserToken?) -> Unit) {
        viewModelScope.launch {
            val token = SharedPreferencesManager.getToken(context)
            val user = token?.let { userRepository.getUserFromToken(it) }
            onResult(user)
        }
    }

    // Función para actualizar datos del perfil del usuario (nombre, apellido, email, etc.)
    fun updateUserProfile(updatedUser: User, context: Context, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val token = SharedPreferencesManager.getToken(context) ?: return@launch onResult(false)
                val response = apiService.updateUserById("Bearer $token", "Bearer $token", updatedUser )
                onResult(response.isSuccessful)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }

    // Función para cambiar la contraseña
    fun changePassword(email: String, newPassword: String, context: Context, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val token = SharedPreferencesManager.getToken(context) ?: return@launch onResult(false)
                val response = apiService.changePassword("Bearer $token", mapOf(
                    "email" to email,
                    "newPassword" to newPassword
                ))
                onResult(response.isSuccessful)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }
    fun refreshToken(context: Context, onResult: (Boolean) -> Unit) {
        val refreshToken = SharedPreferencesManager.getRefreshToken(context) ?: return

        viewModelScope.launch {
            try {
                val refreshToken = SharedPreferencesManager.getRefreshToken(context) ?: return@launch
                val mediaType = "text/plain".toMediaType()
                val requestBody = refreshToken.toRequestBody(mediaType)

                val response = RetrofitInstance.api.refreshToken(requestBody)

                if (response.isSuccessful) {
                    val newToken = response.body()?.token
                    val newRefreshToken = response.body()?.refreshToken

                    if (newToken != null && newRefreshToken != null) {
                        val expirationDate = System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000
                        val obtainedDate = System.currentTimeMillis()

                        SharedPreferencesManager.saveToken(context, newToken, expirationDate, obtainedDate)
                        SharedPreferencesManager.saveRefreshToken(context, newRefreshToken)

                        onResult(true)
                    } else {
                        onResult(false)
                    }
                } else {
                    onResult(false)
                }
            } catch (e: Exception) {
                Log.e("UserViewModel", "Error al refrescar token: ${e.message}")
                onResult(false)
            }
        }
    }

    fun checkTokenExpirationAndRefresh(context: Context, onResult: (Boolean) -> Unit) {
        val obtainedDate = SharedPreferencesManager.getTokenObtainedDate(context)
        val currentTime = System.currentTimeMillis()
        val daysPassed = (currentTime - obtainedDate) / (1000 * 60 * 60 * 24)

        if (daysPassed in 5 until 7) {
            refreshToken(context, onResult)
        } else {
            onResult(false)
        }
    }

    fun onAppLaunch(context: Context) {
        checkTokenExpirationAndRefresh(context) { isRefreshed ->
            if (isRefreshed) {
                Log.d("UserViewModel", "Token refrescado exitosamente.")
            } else {
                Log.d("UserViewModel", "No es necesario refrescar el token.")
            }
        }
    }


}
