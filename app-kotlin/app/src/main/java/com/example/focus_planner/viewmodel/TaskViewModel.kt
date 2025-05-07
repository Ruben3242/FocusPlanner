package com.example.focus_planner.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.focus_planner.data.model.Task
import com.example.focus_planner.data.model.TaskPriority
import com.example.focus_planner.data.model.TaskStatus
import com.example.focus_planner.data.repository.TaskRepository
import com.example.focus_planner.network.ApiService
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
) : ViewModel() {

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
        refreshTasks()
    }
    private fun refreshTasks() {
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




//    fun setSearchQuery(query: String, token: String) {
//        _searchQuery.value = query
//        resetPagination()
//        loadTasks(token)
//    }
//
//    fun setStatusFilter(status: TaskStatus, token: String) {
//        _statusFilter.value = status
//        resetPagination()
//        loadTasks(token)
//    }
//
//    fun setPriorityFilter(priority: TaskPriority, token: String) {
//        _priorityFilter.value = priority
//        resetPagination()
//        loadTasks(token)
//    }

    //detalles de la tarea

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
}
