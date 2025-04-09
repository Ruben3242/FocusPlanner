package com.example.focus_planner.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_user")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "username") val username: String,
    @ColumnInfo(name = "email") val email: String,
    @ColumnInfo(name = "password") val password: String,
    @ColumnInfo(name = "role") val role: String,  // Usamos String para el enum
    @ColumnInfo(name = "firstname") val firstname: String?,
    @ColumnInfo(name = "lastname") val lastname: String?,
    @ColumnInfo(name = "country") val country: String?,
    @ColumnInfo(name = "is_verified") val isVerified: Boolean,
    @ColumnInfo(name = "verification_token") val verificationToken: String?,
    @ColumnInfo(name = "remove_completed_expired_tasks") val removeCompletedExpiredTasks: Boolean
) {
    // Si necesitas relacionar con las tareas, puedes hacerlo en el DAO o usando un ViewModel para manejar la relación
}
