package com.example.focus_planner.viewmodel

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.focus_planner.data.model.UpdateUserRequest
import com.example.focus_planner.data.model.User
import com.example.focus_planner.data.model.UserResponse
import com.example.focus_planner.model.LoginRequest
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
        userId: Long,
        currentUserData: User,
        newPassword: String,
        emailInput: String, // Email ingresado por el usuario
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Verificamos si el correo ingresado es el mismo que el del usuario
                if (currentUserData.email != emailInput) {
                    onError("El correo ingresado no coincide con el correo actual.")
                    return@launch
                }

                // Si los correos son iguales, procedemos con la validación de las contraseñas
                if (newPassword.isEmpty()) {
                    onError("La nueva contraseña no puede estar vacía.")
                    return@launch
                }

                val updateRequest = currentUserData.firstname?.let {
                    currentUserData.lastname?.let { it1 ->
                        UpdateUserRequest(
                            username = currentUserData.username,
                            email = currentUserData.email,
                            firstname = it,
                            lastname = it1,
                            password = newPassword // Actualizamos la contraseña
                        )
                    }
                }

                val response = updateRequest?.let {
                    apiService.updateUserProfile(
                        userId = userId,
                        token = "Bearer $token",
                        request = it
                    )
                }

                if (response != null && response.isSuccessful) {
                    val userResponse = response.body()
                    if (userResponse != null) {
                        _user.value = userResponse.toDomain()
                        onSuccess() // Si todo va bien, ejecutamos onSuccess
                    } else {
                        onError("Error al obtener la respuesta del servidor.")
                    }
                } else {
                    onError("Error al cambiar la contraseña: ${response?.code()}")
                }
            } catch (e: Exception) {
                onError("Error de red: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

//    fun updatePassword(newPassword: String, userId: Long, token: String, context: Context) {
//        viewModelScope.launch {
//            try {
//                // Crea la solicitud con los datos necesarios para actualizar la contraseña
//                val updatedUser = userProfile.value?.firstname?.let {
//                    userProfile.value?.let { it1 ->
//                        userProfile.value!!.lastname?.let { it2 ->
//                            UpdateUserRequest(
//                                username = it1.username, // Mantén el username actual
//                                email = userProfile.value!!.email, // Mantén el email actual
//                                firstname = it,
//                                lastname = it2,
//                                password = newPassword // Establece la nueva contraseña
//                            )
//                        }
//                    }
//                }
//
//                val response =
//                    updatedUser?.let { apiService.updateUserProfile(userId, "Bearer $token", it) }
//
//                if (response != null) {
//                    if (response.isSuccessful) {
//                        // Si la actualización es exitosa, muestra un mensaje de éxito
//                        showPasswordChangeDialog = false
//                        Toast.makeText(context, "Contraseña actualizada exitosamente", Toast.LENGTH_SHORT).show()
//                    } else {
//                        // Si ocurre un error al actualizar, muestra el mensaje de error
//                        Toast.makeText(context, "Error al actualizar la contraseña", Toast.LENGTH_SHORT).show()
//                    }
//                }
//            } catch (e: Exception) {
//                Toast.makeText(context, "Error al actualizar la contraseña: ${e.message}", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }



    fun authenticateUser(email: String, currentPassword: String, onResult: (String?, String?) -> Unit) {
        viewModelScope.launch {
            try {
                // Aquí haces la llamada al backend para autenticar al usuario
                val response = apiService.login(LoginRequest(email, currentPassword))

                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    if (loginResponse != null) {
                        // Si la autenticación es correcta, se devuelve el token
                        onResult(loginResponse.token, null)
                    } else {
                        onResult(null, "Error de autenticación")
                    }
                } else {
                    onResult(null, "Correo o contraseña incorrectos")
                }
            } catch (e: Exception) {
                onResult(null, "Error al intentar iniciar sesión: ${e.message}")
            }
        }
    }



}
