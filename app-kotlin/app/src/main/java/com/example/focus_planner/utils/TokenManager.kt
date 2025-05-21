package com.example.focus_planner.utils

import android.content.Context
import android.util.Base64
import android.util.Log
import androidx.navigation.NavController
import com.example.focus_planner.network.RetrofitInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

object TokenManager {

    fun isTokenExpired(context: Context): Boolean {
        val token = SharedPreferencesManager.getToken(context)
        if (token.isNullOrEmpty()) return true
        return isAccessTokenExpired(token)
    }


    fun isRefreshPeriodExpired(context: Context): Boolean {
        val loginTime = SharedPreferencesManager.getInitialLoginTimestamp(context)
        val sevenDaysMillis = 7 * 24 * 60 * 60 * 1000
        return System.currentTimeMillis() > loginTime + sevenDaysMillis
    }

    fun shouldRefreshToken(context: Context): Boolean {
        val expirationTime = SharedPreferencesManager.getTokenExpirationTime(context)
        val currentTime = System.currentTimeMillis()
        return currentTime >= expirationTime
    }


    fun checkTokenAndRefresh(context: Context, navController: NavController) {
        val accessTokenExpired = isTokenExpired(context)
        val refreshExpired = isRefreshPeriodExpired(context)

        Log.d("TokenDebug", "Access token expired: $accessTokenExpired")
        Log.d("TokenDebug", "Refresh period expired: $refreshExpired")

        if (accessTokenExpired) {
            val refreshToken = SharedPreferencesManager.getRefreshToken(context)
            Log.d("TokenDebug", "Refresh token leído: $refreshToken")

            if (refreshToken.isNullOrEmpty()) {
                Log.e("TokenDebug", "Refresh token está vacío o null. Cancelando.")
                return
            }

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    Log.d("TokenDebug", "Intentando refrescar token...")
                    val refreshToken = SharedPreferencesManager.getRefreshToken(context) ?: return@launch
                    val mediaType = "text/plain".toMediaType()
                    val requestBody = refreshToken.toRequestBody(mediaType)

                    val response = RetrofitInstance.api.refreshToken(requestBody)


                    if (response.isSuccessful) {
                        val newToken = response.body()?.token
                        val newRefresh = response.body()?.refreshToken

                        Log.d("TokenDebug", "Nuevo token: $newToken")
                        Log.d("TokenDebug", "Nuevo refresh token: $newRefresh")

                        if (newToken != null && newRefresh != null) {
                            val newExpiration = System.currentTimeMillis() + 24 * 60 * 60 * 1000
                            SharedPreferencesManager.saveLoginData(context, newToken, newRefresh, newExpiration)
                            Log.d("TokenDebug", "Token actualizado correctamente")
                        } else {
                            Log.e("TokenDebug", "Token o refreshToken nulos en la respuesta.")
                            clearSessionIfExpired(context, navController)
                        }
                    } else {
                        Log.e("TokenDebug", "Error de backend: código ${response.code()}, cuerpo: ${response.errorBody()?.string()}")
                        clearSessionIfExpired(context, navController)
                    }
                } catch (e: Exception) {
                    Log.e("TokenDebug", "Excepción al refrescar token: ${e.message}", e)
                    clearSessionIfExpired(context, navController)
                }
            }
        } else {
            if (refreshExpired) {
                Log.d("TokenDebug", "Periodo de refresh expirado. Cerrando sesión.")
                clearSessionIfExpired(context, navController)
            } else {
                Log.d("TokenDebug", "Token aún válido.")
            }
        }
    }



    fun clearSessionIfExpired(context: Context, navController: NavController) {
        SharedPreferencesManager.clearSession(context)
        CoroutineScope(Dispatchers.Main).launch {
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    fun isAccessTokenExpired(token: String): Boolean {
        try {
            val parts = token.split(".")
            if (parts.size < 3) return true

            val payload = String(Base64.decode(parts[1], Base64.URL_SAFE))
            val json = JSONObject(payload)
            val exp = json.getLong("exp") // En segundos

            val currentTime = System.currentTimeMillis() / 1000 // También en segundos
            return exp < currentTime
        } catch (e: Exception) {
            Log.e("TokenDebug", "Error al verificar expiración: ${e.message}")
            return true
        }
    }


}
