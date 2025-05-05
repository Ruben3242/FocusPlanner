package com.example.focus_planner.utils

import android.content.Context
import android.util.Log
import androidx.navigation.NavController
import com.example.focus_planner.network.RetrofitInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

object TokenManager {

    fun isTokenExpired(context: Context): Boolean {
        val expiration = SharedPreferencesManager.getTokenExpiration(context)
        return System.currentTimeMillis() > expiration
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

//    fun checkTokenAndRefresh(context: Context, navController: NavController) {
//        Log.d("TokenRefresh", "Comprobando si el token ha caducado...")
//
//        if (isTokenExpired(context)) {
//            Log.d("TokenRefresh", "El token ha caducado. Intentando renovarlo...")
//
//            val refreshToken = SharedPreferencesManager.getRefreshToken(context)
//            if (refreshToken == null) {
//                Log.w("TokenRefresh", "No se encontró refresh token.")
//                return
//            }
//
//            CoroutineScope(Dispatchers.IO).launch {
//                try {
//                    val response = RetrofitInstance.api.refreshToken(refreshToken)
//                    if (response.isSuccessful) {
//                        val newToken = response.body()?.token
//                        val newRefresh = response.body()?.refreshToken
//                        val newExpiration = System.currentTimeMillis() + 24 * 60 * 60 * 1000 // 24 horas
//
//                        if (newToken != null && newRefresh != null) {
//                            SharedPreferencesManager.saveLoginData(context, newToken, newRefresh, newExpiration)
//                            Log.d("TokenRefresh", "Token actualizado correctamente con nuevo token y refresh token.")
//                        } else {
//                            Log.w("TokenRefresh", "Respuesta sin tokens válidos. Cerrando sesión.")
//                            clearSessionIfExpired(context, navController)
//                        }
//                    } else {
//                        Log.w("TokenRefresh", "Respuesta no exitosa al renovar token. Código: ${response.code()}")
//                        clearSessionIfExpired(context, navController)
//                    }
//                } catch (e: Exception) {
//                    Log.e("TokenRefresh", "Error al renovar token: ${e.message}")
//                    clearSessionIfExpired(context, navController)
//                }
//            }
//        } else {
//            Log.d("TokenRefresh", "El token aún es válido. Verificando si expiró el periodo de 7 días...")
//
//            if (isRefreshPeriodExpired(context)) {
//                Log.w("TokenRefresh", "El periodo de 7 días ha expirado. Cerrando sesión.")
//                clearSessionIfExpired(context, navController)
//            } else {
//                Log.d("TokenRefresh", "Todo correcto. No es necesario renovar ni cerrar sesión.")
//            }
//        }
//    }

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
//                            SharedPreferencesManager.saveLoginData(context, newToken, newRefresh, newExpiration)
                            SharedPreferencesManager.saveLoginData(
                                context,
                                newToken,
                                refreshToken,
                                System.currentTimeMillis() - (8 * 24 * 60 * 60 * 1000) // hace 2 días
                            )
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

}
