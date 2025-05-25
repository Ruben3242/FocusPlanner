package com.example.focus_planner.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.example.focus_planner.utils.SharedPreferencesManager.KEY_PROFILE_IMAGE_URI
import com.example.focus_planner.utils.SharedPreferencesManager.PREFS_NAME
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class ProfileImageViewModel@Inject constructor(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _profileImageUri = MutableStateFlow<Uri?>(loadUriFromPrefs())
    val profileImageUri: StateFlow<Uri?> = _profileImageUri

    fun updateProfileImage(uri: Uri?) {
        Log.d("ProfileImageViewModel", "Actualizando imagen: $uri")
        saveUriToPrefs(uri)
        _profileImageUri.value = uri
    }


    private fun saveUriToPrefs(uri: Uri?) {
        prefs.edit().putString(KEY_PROFILE_IMAGE_URI, uri?.toString()).apply()
    }

    private fun loadUriFromPrefs(): Uri? {
        val uriString = prefs.getString(KEY_PROFILE_IMAGE_URI, null)
        return uriString?.let { Uri.parse(it) }
    }
}