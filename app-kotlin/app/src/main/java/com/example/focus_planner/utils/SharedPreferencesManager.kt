package com.example.focus_planner.utils

import android.content.Context
import android.util.Log

object SharedPreferencesManager {
    private const val PREFS_NAME = "focus_planner_prefs"
    private const val TOKEN_KEY = "auth_token"
    private const val EXPIRATION_KEY = "token_expiration"

    // Guardar token y fecha de expiración
    fun saveToken(context: Context, token: String?, expirationDate: Long) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(TOKEN_KEY, token)
        editor.putLong(EXPIRATION_KEY, expirationDate)
        editor.apply()

        // Agrega un log para verificar si el token y la fecha de expiración se guardaron correctamente
        Log.d("SharedPreferencesManager", "Token guardado: $token, Expiración: $expirationDate")
    }


    // Obtener token guardado
    fun getToken(context: Context): String? {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val token = sharedPreferences.getString(TOKEN_KEY, null)
        Log.d("SharedPreferencesManager", "Token recuperado: $token")
        return token
    }


    // Obtener la fecha de expiración guardada
    fun getTokenExpiration(context: Context): Long {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getLong(EXPIRATION_KEY, System.currentTimeMillis()) // Retorna la fecha actual si no se ha guardado ninguna.
    }


    // Verificar si el token ha expirado
    fun isTokenExpired(context: Context): Boolean {
        val expirationDate = getTokenExpiration(context)
        val expired = System.currentTimeMillis() > expirationDate
        Log.d("SharedPreferencesManager", "Token expirado: $expired, Fecha de expiración: $expirationDate")
        return expired
    }

    // Eliminar token y fecha de expiración (para logout)
    fun clearToken(context: Context) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove(TOKEN_KEY)
        editor.remove(EXPIRATION_KEY)
        editor.apply()
    }

}
