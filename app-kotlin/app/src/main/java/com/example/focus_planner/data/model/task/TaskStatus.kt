package com.example.focus_planner.data.model.task

enum class TaskStatus(val value: String) {
    PENDING("PENDING"),
    COMPLETED("COMPLETED"),
    EXPIRED("EXPIRED"),
    COMPLETED_OR_EXPIRED("COMPLETED_OR_EXPIRED")
}
enum class TaskPriority(val value: String) {
    LOW("LOW"),
    MEDIUM("MEDIUM"),
    HIGH("HIGH")
}