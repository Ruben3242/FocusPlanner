package com.example.focus_planner.utils

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.example.focus_planner.utils.SharedPreferencesManager.getPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PreferencesManager(context: Context) {
    private val Context.dataStore by preferencesDataStore("settings")
    private val dataStore = context.dataStore

    companion object {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { it[ONBOARDING_COMPLETED] = completed }
    }

    val onboardingCompleted: Flow<Boolean> = dataStore.data
        .map { it[ONBOARDING_COMPLETED] ?: false }

    fun isOnboardingCompleted(context: Context): Boolean {
        val sharedPreferences = getPreferences(context)
        return sharedPreferences.getBoolean("onboarding_completed", false)
    }

    fun setOnboardingCompleted(context: Context, completed: Boolean) {
        val sharedPreferences = getPreferences(context)
        sharedPreferences.edit().putBoolean("onboarding_completed", completed).apply()
    }

}
