package com.example.focus_planner.viewmodel

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.focus_planner.data.model.UpdateUserRequest
import com.example.focus_planner.data.model.User
import com.example.focus_planner.network.ApiService
import com.example.focus_planner.utils.SharedPreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

//    private val _user = mutableStateOf<User?>(null)
//    val user: State<User?> = _user
//    var isLoading by mutableStateOf(false)
//    var errorMessage by mutableStateOf<String?>(null)
//
//    // Función para cargar los datos del usuario
//    fun loadUser(userId: Long, token: String) {
//        viewModelScope.launch {
//            isLoading = true
//            val response = apiService.getUserById(userId, "Bearer $token")
//            if (response.isSuccessful) {
//                _user.value = response.body()
//                errorMessage = null
//            } else {
//                errorMessage = "Error cargando el perfil"
//            }
//            isLoading = false
//        }
//    }
//
//    // Función para actualizar los datos del usuario
//    fun updateUser(userId: Long, token: String, updatedUser: User, onSuccess: () -> Unit, onError: (String) -> Unit) {
//        viewModelScope.launch {
//            isLoading = true
//            val response = apiService.updateUserById(userId, "Bearer $token", updatedUser)
//            if (response.isSuccessful) {
//                _user.value = response.body() // Actualizamos los datos del usuario en el perfil
//                onSuccess()
//            } else {
//                onError("Error al actualizar el perfil")
//            }
//            isLoading = false
//        }
//    }
//    fun fetchUserProfile(token: String) {
//        viewModelScope.launch {
//            try {
//                val response = apiService.getUserProfile("Bearer $token")
//                if (response.isSuccessful) {
//                    _user.value = response.body()
//                } else {
//                    Log.e("ProfileViewModel", "Error al obtener el perfil: ${response.code()}")
//                }
//            } catch (e: Exception) {
//                Log.e("ProfileViewModel", "Excepción: ${e.message}")
//            }
//        }
//    }

    // Usamos StateFlow para manejar el estado del perfil
    private val _userProfile = MutableStateFlow<User?>(null)
    val userProfile: StateFlow<User?> = _userProfile

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    // Obtener perfil del usuario
    fun getUserProfile(token: String) {
        viewModelScope.launch {
            Log.d("ProfileViewModel", "Token recibido: $token")  // Log del token recibido

            try {
                val response = apiService.getUserProfile("Bearer $token")
                Log.d("ProfileViewModel", "Respuesta recibida: code=${response.code()}, isSuccessful=${response.isSuccessful}")

                if (response.isSuccessful) {
                    val userBody = response.body()
                    if (userBody != null) {
                        _userProfile.value = userBody
                        Log.d("ProfileViewModel", "Perfil obtenido correctamente: ${userBody.email}")
                    } else {
                        Log.e("ProfileViewModel", "El cuerpo de la respuesta es null")
                    }
                } else {
                    Log.e("ProfileViewModel", "Error al obtener el perfil: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error de red o servidor: ${e.message}")
            }
        }
    }


    fun updateUserProfile(
        userId: Long,
        updatedUser: UpdateUserRequest,
        context: Context,
        navController: NavController
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // 🔐 Obtener el token actualizado desde SharedPreferences
                val token = SharedPreferencesManager.getToken(context)
                if (token.isNullOrEmpty()) {
                    _errorMessage.value = "Token no encontrado. Por favor, inicia sesión nuevamente."
                    Log.e("ProfileViewModel", "Token vacío. No se puede actualizar el perfil.")
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                    return@launch
                }

                val oldEmail = _userProfile.value?.email
                Log.d("ProfileViewModel", "Email anterior: $oldEmail")

                val response = apiService.updateUserProfile(userId, "Bearer $token", updatedUser)
                if (response.isSuccessful) {
                    val userResponse = response.body()
                    _userProfile.value = userResponse?.toDomain()
                    Log.d("ProfileViewModel", "Perfil actualizado exitosamente.")

                    val newEmail = userResponse?.email
                    Log.d("ProfileViewModel", "Nuevo email: $newEmail")
                    if (oldEmail != null && newEmail != null && oldEmail != newEmail) {
                        Log.d("ProfileViewModel", "El correo cambió, cerrando sesión y redirigiendo al login.")
                        SharedPreferencesManager.saveUserEmail(context, newEmail)
                        SharedPreferencesManager.clearSession(context)
                        delay(2000)
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    } else {
                        Log.d("ProfileViewModel", "No hubo cambio en el correo, redirigiendo a la home.")
                        navController.navigate("home") {
                            popUpTo("profile") { inclusive = true }
                        }
                    }
                } else {
                    _errorMessage.value = "Error al actualizar el perfil: ${response.code()}"
                    Log.e("ProfileViewModel", "Error al actualizar el perfil: ${response.code()}")
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error de red: ${e.message}"
                Log.e("ProfileViewModel", "Error de red: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }






    fun changePassword(
        token: String,
        oldPassword: String,
        newPassword: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiService.changePassword(
                    "Bearer $token",
                    mapOf("oldPassword" to oldPassword, "newPassword" to newPassword)
                )
                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    onError("Error al cambiar la contraseña")
                }
            } catch (e: Exception) {
                onError("Error de red: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

}
