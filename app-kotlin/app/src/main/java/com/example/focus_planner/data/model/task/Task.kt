package com.example.focus_planner.data.model.task

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.example.focus_planner.data.model.User

@Entity(tableName = "task_table")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "description") val description: String,
    @ColumnInfo(name = "completed") val completed: Boolean,
    @ColumnInfo(name = "due_date") val dueDate: String?,  // Usamos Long para las fechas
    @ColumnInfo(name = "status") val status: TaskStatus,  // Usamos String para el enum
    @ColumnInfo(name = "priority") val priority: TaskPriority,  // Usamos String para el enum
    @ColumnInfo(name = "google_calendar_event_id") val googleCalendarEventId: String?,
    @ColumnInfo(name = "user_id") val userId: Long,  // Agrega esta columna
    @Relation(parentColumn = "user_id", entityColumn = "id")
    val user: User? = null  // Relación con el usuario
)

data class TaskSummaryDTO(
    val dueDate: String,  // Recibiremos la fecha en formato ISO, la parseamos luego a LocalDate
    val title: String
)


