package com.example.focus_planner.viewmodel

import android.content.Context
import android.os.Environment
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.focus_planner.data.model.UpdateSettingsRequest
import com.example.focus_planner.data.model.User
import com.example.focus_planner.data.model.task.TaskDto
import com.example.focus_planner.data.model.task.TaskStatus
import com.example.focus_planner.data.repository.TaskRepository
import com.example.focus_planner.data.repository.UserRepository
import com.example.focus_planner.network.ApiService
import com.example.focus_planner.utils.SharedPreferencesManager
import com.example.focus_planner.utils.SharedPreferencesManager.clearSession
import com.example.focus_planner.utils.SharedPreferencesManager.getSelectedStatuses
import com.example.focus_planner.utils.SharedPreferencesManager.getToken
import com.example.focus_planner.utils.SharedPreferencesManager.loadAutoDeleteEnabled
import com.example.focus_planner.utils.SharedPreferencesManager.loadSelectedStatuses
import com.example.focus_planner.utils.SharedPreferencesManager.saveSelectedStatuses
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val apiService: ApiService,
    private val userRepository: UserRepository,
    private val taskRepository: TaskRepository,
) : ViewModel() {

    private val _exportedJson = MutableStateFlow<String?>(null)
    val exportedJson: StateFlow<String?> = _exportedJson

    private val _importResult = MutableStateFlow<String?>(null)
    val importResult: StateFlow<String?> = _importResult

    private val _selectedStatuses = mutableStateListOf<TaskStatus>()
    val selectedStatuses: SnapshotStateList<TaskStatus> = _selectedStatuses

    private val _autoDeleteEnabled = mutableStateOf(false)
    val autoDeleteEnabled: State<Boolean> get() = _autoDeleteEnabled

    fun loadSelectedStatuses(context: Context) {
        val saved = getSelectedStatuses(context)
        _selectedStatuses.clear()
        _selectedStatuses.addAll(saved)
    }

    fun toggleStatusSelection(context: Context, status: TaskStatus) {
        if (_selectedStatuses.contains(status)) {
            _selectedStatuses.remove(status)
        } else {
            _selectedStatuses.add(status)
        }
        saveSelectedStatuses(context, _selectedStatuses)
    }

    fun clearSelectedStatuses(context: Context) {
        _selectedStatuses.clear()
        SharedPreferencesManager.clearSelectedStatuses(context)
    }
    fun toggleStatusSelection(status: TaskStatus) {
        if (_selectedStatuses.contains(status)) {
            _selectedStatuses.remove(status)
        } else {
            _selectedStatuses.add(status)
        }
    }

    fun clearSelectedStatuses() {
        _selectedStatuses.clear()
    }


    fun setStatuses(statuses: List<TaskStatus>) {
        _selectedStatuses.clear()
        _selectedStatuses.addAll(statuses)
    }

    fun setAutoDeleteEnabled(value: Boolean) {
        _autoDeleteEnabled.value = value
    }


    fun exportTasks(context: Context) {
        val token = getToken(context)
        Log.d("ExportTasks", "Iniciando export con token: $token")

        viewModelScope.launch {
            try {
                val tasks = apiService.exportTasks("Bearer $token")  // devuelve List<TaskDto>
                Log.d("ExportTasks", "Tareas recibidas: ${tasks.size}")

                val jsonString = com.google.gson.Gson().toJson(tasks)
                Log.d("ExportTasks", "JSON generado: $jsonString")

                _exportedJson.value = jsonString

                // Opcional: prueba guardar en archivo local y loguear
                try {
                    val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    val file = File(downloadsDir, "tasks_export.json")
                    file.writeText(jsonString)
                    Log.d("ExportTasks", "Archivo guardado en: ${file.absolutePath}")
                } catch (e: Exception) {
                    Log.e("ExportTasks", "Error guardando archivo: ${e.message}")
                }

            } catch (e: Exception) {
                Log.e("ExportTasks", "Error exportando tareas: ${e.message}")
                _exportedJson.value = null
            }
        }
    }

    fun importTasks(jsonString: String, context: Context) {
        val token = getToken(context)
        viewModelScope.launch {
            try {
                val gson = GsonBuilder().setLenient().create()
                val taskListType = object : TypeToken<List<TaskDto>>() {}.type
                val tasks: List<TaskDto> = gson.fromJson(jsonString, taskListType)

                Log.d("ImportTasks", "Tareas parseadas: ${tasks.size}")

                val responseBody = apiService.importTasks("Bearer $token", tasks)
                val responseString = responseBody.string() // aquí el texto plano

                _importResult.value = responseString
                Log.d("ImportTasks", "Respuesta import: $responseString")
            } catch (e: Exception) {
                _importResult.value = "Error al importar tareas: ${e.message}"
                Log.e("ImportTasks", "Error al importar tareas", e)
            }
        }
    }



    fun clearExportedJson() {
        _exportedJson.value = null
    }

    fun clearImportResult() {
        _importResult.value = null
    }

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    private val _deleteResult = mutableStateOf<String?>(null)
    val deleteResult: State<String?> get() = _deleteResult

    fun deleteTasksByStatuses(token: String, statuses: List<TaskStatus>) {
        Log.d("DEBUG", "Llamando a deleteTasksByStatuses con $statuses")
        Log.d("DEBUG", "Token: $token")
        viewModelScope.launch {
            try {
                val response = taskRepository.deleteTasksByStatuses(token, statuses)
                Log.d("DEBUG", "Respuesta de deleteTasksByStatuses: $response")
                _deleteResult.value = response
            } catch (e: Exception) {
                Log.e("DEBUG", "Error al eliminar tareas: ${e.message}")
                _deleteResult.value = "Error al eliminar tareas"
            }
        }
    }



    fun updateUserSettings(userId: Long, token: String, autoDeleteEnabled: Boolean) {
        viewModelScope.launch {
            try {
                val request = UpdateSettingsRequest(removeCompletedExpiredTasks = autoDeleteEnabled)
                Log.d("Settings", "Llamando al endpoint con: $autoDeleteEnabled")
                val response = userRepository.updateUserById(userId.toString(), token, request)

                if (response.isSuccessful) {
                    response.body()?.let {
                        _user.value = it
                        Log.d("Settings", "Actualización exitosa")
                    }
                } else {
                    Log.e("Settings", "Falló la actualización: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("Settings", "Excepción al actualizar usuario", e)
            }
        }
    }


    fun loadUserById(userId: Long, token: String) {
        viewModelScope.launch {
            try {
                val response = userRepository.getUserById(userId, token)
                if (response.isSuccessful) {
                    _user.value = response.body()
                } else {
                    // Maneja error, por ejemplo log o mensaje
                    Log.e("SettingsViewModel", "Error al cargar usuario: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("SettingsViewModel", "Excepción al cargar usuario", e)
            }
        }
    }
    private val _deleteSuccess = MutableStateFlow(false)
    val deleteSuccess: StateFlow<Boolean> = _deleteSuccess

    fun deleteUserAccount(token: String, context: Context) {
        viewModelScope.launch {
            try {
                userRepository.deleteAccount("Bearer $token")
                clearSession(context)
                _deleteSuccess.value = true
            } catch (e: Exception) {
                Log.e("DeleteAccount", "Error al eliminar cuenta: ${e.message}")
            }
        }
    }
}
