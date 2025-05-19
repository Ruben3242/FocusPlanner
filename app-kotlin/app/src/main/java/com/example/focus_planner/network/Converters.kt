package com.example.focus_planner.network

import androidx.room.TypeConverter
import com.example.focus_planner.data.model.task.TaskPriority
import com.example.focus_planner.data.model.task.TaskStatus

class Converters {
    @TypeConverter
    fun fromStatus(value: TaskStatus): String = value.name

    @TypeConverter
    fun toStatus(value: String): TaskStatus = TaskStatus.valueOf(value)

    @TypeConverter
    fun fromPriority(value: TaskPriority): String = value.name

    @TypeConverter
    fun toPriority(value: String): TaskPriority = TaskPriority.valueOf(value)
}
