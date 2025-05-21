package com.example.focus_planner.viewmodel

import android.content.Context
import android.os.Environment
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.focus_planner.data.model.task.TaskDto
import com.example.focus_planner.network.ApiService
import com.example.focus_planner.utils.SharedPreferencesManager.getToken
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(private val apiService: ApiService) : ViewModel() {

    private val _exportedJson = MutableStateFlow<String?>(null)
    val exportedJson: StateFlow<String?> = _exportedJson

    private val _importResult = MutableStateFlow<String?>(null)
    val importResult: StateFlow<String?> = _importResult

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
}
