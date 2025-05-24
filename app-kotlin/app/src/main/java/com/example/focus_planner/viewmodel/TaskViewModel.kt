package com.example.focus_planner.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.focus_planner.data.model.task.Task
import com.example.focus_planner.data.repository.TaskRepository
import com.example.focus_planner.network.ApiService
import com.example.focus_planner.utils.SharedPreferencesManager.getToken
import com.example.focus_planner.utils.SharedPreferencesManager.getUserId
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val repository: TaskRepository,
    private val api: ApiService
) : ViewModel() {


    private val _taskDeleted = MutableStateFlow<Boolean?>(null)
    val taskDeleted: StateFlow<Boolean?> = _taskDeleted
    private val _taskList = MutableStateFlow<List<Task>>(emptyList())
    val taskList: StateFlow<List<Task>> = _taskList

    private val _selectedTask = MutableStateFlow<Task?>(null)
    val selectedTask: StateFlow<Task?> = _selectedTask

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks


    private val _searchQuery = MutableStateFlow("")
    private val _statusFilter = MutableStateFlow<String?>(null)
    private val _priorityFilter = MutableStateFlow<String?>(null)
    private val _showCompleted = MutableStateFlow<Boolean?>(null)
    private val _page = MutableStateFlow(0)
    private val _token = MutableStateFlow("")


    private var currentToken: String = ""

    fun initializeFiltering() {
        combine(
            _searchQuery,
            _statusFilter,
            _priorityFilter,
            _showCompleted,
            _page
        ) { title, status, priority, completed, page ->
            loadTasks(
                token = currentToken,
                title = title,
                status = status,
                priority = priority,
                completed = completed,
                page = page
            )
        }.launchIn(viewModelScope)
    }

    fun setToken(token: String) {
        _token.value = token
        currentToken = token
    }
    fun refreshTasks() {
        _page.value = 0
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        resetPagination()
    }

    fun onStatusFilterChange(status: String?) {
        _statusFilter.value = status
        resetPagination()
    }

    fun onPriorityFilterChange(priority: String?) {
        _priorityFilter.value = priority
        resetPagination()
    }

    fun setShowCompleted(show: Boolean) {
        _showCompleted.value = if (show) null else false
        resetPagination()
    }

    private fun resetPagination() {
        _page.value = 0
        _tasks.value = emptyList()
    }

    fun loadNextPage() {
        _page.value = _page.value + 1
    }

    private fun loadTasks(
        token: String,
        title: String?,
        status: String?,
        priority: String?,
        completed: Boolean?,
        page: Int
    ) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val response = repository.getFilteredTasks(
                    token = token,
                    title = if (title.isNullOrBlank()) null else title,
                    status = status,
                    priority = priority,
                    completed = completed,
                    page = page,
                    size = 5
                )
                if (response.isSuccessful) {
                    val newTasks = response.body() ?: emptyList()
                    _tasks.value = if (page == 0) newTasks else _tasks.value + newTasks
                } else {
                    Log.e("TaskViewModel", "Error: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("TaskViewModel", "Exception loading tasks", e)
            } finally {
                _loading.value = false
            }
        }
    }



    fun deleteTasksByStatuses(context: Context, statuses: List<String>) {
        val token = getToken(context)
        viewModelScope.launch {
            _loading.value = true
            try {
                val response = api.deleteTasksByStatuses("Bearer $token", statuses)
                if (response.isSuccessful) {
                    Log.d("TaskViewModel", "Tareas eliminadas con estados: $statuses")
                    loadTasks(
                        token = token ?: "",
                        title = _searchQuery.value,
                        status = _statusFilter.value,
                        priority = _priorityFilter.value,
                        completed = _showCompleted.value,
                        page = _page.value
                    )
                } else {
                    Log.e("TaskViewModel", "Error al eliminar tareas: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("TaskViewModel", "Exception en deleteTasksByStatuses", e)
            } finally {
                _loading.value = false
            }
        }
    }


    // Función para obtener los detalles de la tarea
    fun loadTaskDetails(taskId: Long, token: String) {
        viewModelScope.launch {
            val result = repository.fetchTaskDetails(taskId, token)
            result.onSuccess { task ->
                _selectedTask.value = task
            }
            result.onFailure {
                // Manejar el error, como mostrar un mensaje de error
                _selectedTask.value = null
            }
        }
    }
    //Funcion para eliminar una tarea
    fun deleteTask(context: Context, taskId: Long) {
        val token = getToken(context)
        if (token == null) {
            _taskDeleted.value = false
            return
        }


        viewModelScope.launch {
            val success = repository.deleteTaskById(taskId, token)
            _taskDeleted.value = success

            loadTasks(
                token = token,
                title = _searchQuery.value,
                status = _statusFilter.value,
                priority = _priorityFilter.value,
                completed = _showCompleted.value,
                page = _page.value
            )
            refreshTasks()
        }
    }

    fun resetTaskDeletedFlag() {
        _taskDeleted.value = null
    }

    private val _creationSuccess = MutableStateFlow<Boolean?>(null)
    val creationSuccess: StateFlow<Boolean?> = _creationSuccess

    fun createTask(task: Task, context: Context) {
        viewModelScope.launch {
            val token = getToken(context)
            val userId = getUserId(context)

            Log.d("CreateTask", "Token obtenido: $token UserId: $userId")

            if (token == null || userId == null) {
                _creationSuccess.value = false
                return@launch
            }

            val taskWithUser = task.copy(userId = userId)

            val response = repository.createTask(taskWithUser, token)
            if (response.isSuccessful) {
                _creationSuccess.value = true
            } else {
                _creationSuccess.value = false
                Log.e("CreateTask", "Error en la respuesta: ${response.code()} ${response.message()}")

            }
        }
    }

    fun resetState() {
        _creationSuccess.value = null
    }


    private val _taskDetail = MutableStateFlow<Task?>(null)
    val taskDetail: StateFlow<Task?> = _taskDetail

    fun loadTaskDetail(taskId: Long, context: Context) {
        viewModelScope.launch {
            val token = getToken(context)
            val task = token?.let { repository.getTaskById(taskId, it) }
            _taskDetail.value = task
        }
    }

    fun markTaskAsCompleted(context: Context) {
        viewModelScope.launch {
            _taskDetail.value?.let { currentTask ->
                val token = getToken(context)
                val updatedTask = currentTask.copy(completed = true)
                if (token != null) {
                    repository.updateTask(updatedTask, token)?.let {
                        _taskDetail.value = it
                    }
                }
            }
        }
    }
    fun updateTaskDueDate(taskId: Long, newDate: String, context: Context) {
        viewModelScope.launch {
            val token = getToken(context)
            if (token != null) {
                val task = repository.getTaskById(taskId, token)
                if (task != null) {
                    val updatedTask = task.copy(dueDate = newDate)
                    repository.updateTask(updatedTask, token)
                    _taskDetail.value = updatedTask
                }
            }
        }
    }



    fun resetTaskDeleted() {
        _taskDeleted.value = null
    }

    fun updateTask(task: Task, context: Context) {
        viewModelScope.launch {
            val token = getToken(context)
            if (token != null) {
                val updatedTask = getUserId(context)?.let { task.copy(userId = it) }
                if (updatedTask != null) {
                    repository.updateTask(updatedTask, token)
                }
            }
        }
    }

    fun createTaskWithAttachments(
        task: Task,
        context: Context,
        imageUri: Uri? = null,
        videoUri: Uri? = null,
        audioUri: Uri? = null
    ) {
        viewModelScope.launch {
            val token = getToken(context)
            val userId = getUserId(context)

            Log.d("CreateTask", "Token obtenido: $token UserId: $userId")

            if (token == null || userId == null) {
                _creationSuccess.value = false
                return@launch
            }

            // Añadimos userId a la tarea, como antes
            val taskWithUser = task.copy(userId = userId)

            // Enviamos al backend SIN añadir los URIs porque no los maneja el backend
            val response = repository.createTask(taskWithUser, token)

            if (response.isSuccessful) {
                _creationSuccess.value = true

                // Aquí guardas localmente los URIs si usas Room o SQLite local,
                // o haces lo que necesites para el frontend (no se envían al backend).
                // Por ejemplo, un método para guardar URIs asociados a esa tarea localmente:
                saveAttachmentsLocally(taskWithUser.id, imageUri, videoUri, audioUri)

            } else {
                _creationSuccess.value = false
                Log.e("CreateTask", "Error en la respuesta: ${response.code()} ${response.message()}")
            }
        }
    }

    // Función de ejemplo para guardar URIs localmente, debes implementarla según tu DB local
    private fun saveAttachmentsLocally(
        taskId: Long,
        imageUri: Uri?,
        videoUri: Uri?,
        audioUri: Uri?
    ) {
        // Ejemplo: guardas en la DB local el taskId con esos URIs
        // Aquí deberías implementar la lógica concreta de Room/SQLite para actualizar la tarea local
        Log.d("SaveAttachments", "Guardando adjuntos locales para tarea $taskId: image=$imageUri, video=$videoUri, audio=$audioUri")
    }

}
