package com.example.focus_planner.data.repository

import android.util.Log
import com.example.focus_planner.data.model.task.Task
import com.example.focus_planner.data.model.task.TaskStatus
import com.example.focus_planner.data.model.task.TaskSummaryDTO
import com.example.focus_planner.data.model.task.UserStatsResponse
import com.example.focus_planner.network.ApiService
import org.threeten.bp.LocalDate
import retrofit2.Response
import javax.inject.Inject

class TaskRepository @Inject constructor(private val api: ApiService) {

    suspend fun getFilteredTasks(
        token: String,
        title: String?,
        status: String?,
        priority: String?,
        completed: Boolean?,
        page: Int,
        size: Int
    ): Response<List<Task>> {
        return api.getFilteredTasks(
            token = "Bearer $token",
            title = title,
            status = status,
            priority = priority,
            completed = completed,
            page = page,
            size = size
        )
    }


    suspend fun fetchTaskDetails(taskId: Long, token: String): Result<Task> {
        return try {
            val response = api.getTaskDetails(taskId, token)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al obtener la tarea"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteTaskById(taskId: Long, token: String): Boolean {
        return try {
            val response = api.deleteTaskById(taskId, "Bearer $token")
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    suspend fun createTask(task: Task, token: String): Response<Task> {
        return api.createTask(task, "Bearer $token")
    }

    suspend fun getTaskById(taskId: Long, token: String): Task? {
        val response = api.getTaskById(taskId, "Bearer $token")
        return if (response.isSuccessful) response.body() else null
    }

    suspend fun updateTask(task: Task, token: String): Task? {
        val response = api.updateTask(task.id, "Bearer $token", task)
        return if (response.isSuccessful) response.body() else null
    }
    suspend fun getTasksByDateRange(startDate: LocalDate, endDate: LocalDate, token: String): List<Task> {
        return api.getTasksByDateRange(startDate.toString(), endDate.toString(),"Bearer $token")
    }

    suspend fun deleteTasksByStatuses(token: String, statuses: List<TaskStatus>): String {
        val statusStrings = statuses.map { it.name }  // Convertir a String
        val response = api.deleteTasksByStatuses("Bearer $token", statusStrings)
        return if (response.isSuccessful) {
            response.body()?.string() ?: "Sin respuesta"
        } else {
            "Error al eliminar tareas"
        }
    }

    suspend fun getUserStats(userId: Long, token: String): UserStatsResponse {
        return api.getUserStats(userId, "Bearer $token")
    }

}