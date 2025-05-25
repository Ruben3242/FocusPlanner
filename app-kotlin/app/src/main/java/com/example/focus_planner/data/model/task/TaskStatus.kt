package com.example.focus_planner.data.model.task

enum class TaskStatus(val value: String, val displayName: String) {
    PENDING("PENDING", "Pendiente"),
    COMPLETED("COMPLETED", "Completada"),
    EXPIRED("EXPIRED", "Expirada"),
    COMPLETED_OR_EXPIRED("COMPLETED_OR_EXPIRED", "Completada y Expirada")
}

enum class TaskPriority(val value: String, val displayName: String) {
    LOW("LOW", "Baja"),
    MEDIUM("MEDIUM", "Media"),
    HIGH("HIGH", "Alta")
}
