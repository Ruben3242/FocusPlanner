package com.example.focus_planner.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import com.auth0.jwt.JWT
import com.auth0.jwt.interfaces.DecodedJWT
import com.example.focus_planner.data.model.task.TaskStatus
import kotlin.math.E

object SharedPreferencesManager {
    private const val PREFS_NAME = "focus_planner_prefs"
    private const val TOKEN_KEY = "auth_token"
    private const val REFRESH_TOKEN_KEY = "refresh_token"
    private const val EXPIRATION_KEY = "token_expiration"
    private const val LOGIN_TIMESTAMP_KEY = "login_timestamp"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USER_EMAIL = "user_email"
    private const val KEY_SELECTED_STATUSES = "selected_statuses"


    // Guardar token y fecha de expiración
    fun saveToken(context: Context, token: String?, expirationDate: Long, obtainedDate: Long) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(TOKEN_KEY, token)
        editor.putLong(EXPIRATION_KEY, expirationDate)
        editor.putLong("token_obtained_date", obtainedDate) // Guardamos la fecha de obtención
        editor.apply()

        Log.d("SharedPreferencesManager", "Token guardado: $token, Expiración: $expirationDate, Obtención: $obtainedDate")
    }
    fun saveRefreshToken(context: Context, refreshToken: String) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(REFRESH_TOKEN_KEY, refreshToken)
        editor.apply()
    }

    // Guardar token, refreshToken, fecha de expiración y login timestamp
    fun saveLoginData(context: Context, token: String, refreshToken: String, tokenExpiration: Long) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(TOKEN_KEY, token)
            .putString(REFRESH_TOKEN_KEY, refreshToken)
            .putLong(EXPIRATION_KEY, tokenExpiration)
//            .putLong(KEY_USER_ID, 0L) // Guardar ID de usuario como 0L por defecto
            .putLong(LOGIN_TIMESTAMP_KEY, System.currentTimeMillis())
            .apply()
    }

    fun getInitialLoginTimestamp(context: Context): Long {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getLong(LOGIN_TIMESTAMP_KEY, 0L)
    }
    // Obtener token guardado
    fun getToken(context: Context): String? {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString(TOKEN_KEY, null)
    }

    // Obtener refreshToken guardado
    fun getRefreshToken(context: Context): String? {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString(REFRESH_TOKEN_KEY, null)
    }

    // Obtener la fecha de expiración guardada
    fun getTokenExpiration(context: Context): Long {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getLong(EXPIRATION_KEY, System.currentTimeMillis())
    }

    // Obtener el timestamp del login
    fun getLoginTimestamp(context: Context): Long {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getLong(LOGIN_TIMESTAMP_KEY, 0L)
    }

    // Verificar si el token ha expirado
    fun isTokenExpired(context: Context): Boolean {
        val expirationDate = getTokenExpiration(context)
        return System.currentTimeMillis() > expirationDate
    }

    // Verificar si han pasado más de 7 días desde el login
    fun isSessionExpired(context: Context): Boolean {
        val loginTimestamp = getLoginTimestamp(context)
        return (System.currentTimeMillis() - loginTimestamp) > (7 * 24 * 60 * 60 * 1000) // 7 días en milisegundos
    }

    // Eliminar token, refreshToken y fecha de expiración (para logout)
    fun clearSession(context: Context) {
        val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sharedPrefs.edit().clear().apply()  // Limpiar todas las preferencias
    }

    // Eliminar token y fecha de expiración (para logout)
    fun clearToken(context: Context) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove(TOKEN_KEY)
        editor.remove(EXPIRATION_KEY)
        editor.apply()
    }


    // Guardar ID de usuario
    fun saveUserId(context: Context, userId: Long) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putLong(KEY_USER_ID, userId).apply()
        Log.d("SharedPreferencesManager", "User ID guardado: $userId")
    }

    // Obtener ID de usuario
    fun getUserId(context: Context): Long? {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return if (sharedPreferences.contains(KEY_USER_ID)) {
            sharedPreferences.getLong(KEY_USER_ID, 0L)
        } else {
            null
        }
    }
    fun saveUserEmail(context: Context, email: String) {
        val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sharedPrefs.edit().putString(KEY_USER_EMAIL, email).apply()
    }

    fun getUserEmail(context: Context): String? {
        val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPrefs.getString(KEY_USER_EMAIL, null)
    }

//    // Eliminar toda la sesión (logout)
//    fun clearSession(context: Context) {
//        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
//            .edit().clear().apply()
//        Log.d("SharedPreferencesManager", "Sesión limpia (logout)")
//    }

    fun getTokenObtainedDate(context: Context): Long {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getLong("token_obtained_date", System.currentTimeMillis())
    }
    fun getRefreshTokenExpirationTime(context: Context): Long {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getLong(EXPIRATION_KEY, System.currentTimeMillis())
    }
    fun getTokenExpirationTime(context: Context): Long {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getLong(EXPIRATION_KEY, 0)
    }

    fun saveAutoDeleteEnabled(context: Context, value: Boolean) {
        context.getSharedPreferences("settings", Context.MODE_PRIVATE)
            .edit()
            .putBoolean("auto_delete", value)
            .apply()
    }

    fun loadAutoDeleteEnabled(context: Context): Boolean {
        return context.getSharedPreferences("settings", Context.MODE_PRIVATE)
            .getBoolean("auto_delete", false)
    }


    fun loadSelectedStatuses(context: Context): List<TaskStatus> {
        val saved = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
            .getStringSet("selected_statuses", emptySet()) ?: emptySet()
        return saved.mapNotNull { runCatching { TaskStatus.valueOf(it) }.getOrNull() }
    }
    fun saveSelectedStatuses(context: Context, statuses: List<TaskStatus>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        val statusNames = statuses.joinToString(",") { it.name } // Guardamos como "PENDING,DONE"
        editor.putString(KEY_SELECTED_STATUSES, statusNames)
        editor.apply()
    }

    fun getSelectedStatuses(context: Context): List<TaskStatus> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val statusNames = prefs.getString(KEY_SELECTED_STATUSES, "") ?: ""
        return statusNames.split(",").mapNotNull {
            try {
                TaskStatus.valueOf(it)
            } catch (e: IllegalArgumentException) {
                null
            }
        }
    }

    fun clearSelectedStatuses(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_SELECTED_STATUSES).apply()
    }

    fun saveProfileImageUri(context: Context, uri: Uri?) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString("profile_image_uri", uri?.toString()).apply()
    }

    fun loadProfileImageUri(context: Context): Uri? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val uriString = prefs.getString("profile_image_uri", null)
        return uriString?.let { Uri.parse(it) }
    }

    //guardar las imagener vidos y aidios de cada tarea




}
