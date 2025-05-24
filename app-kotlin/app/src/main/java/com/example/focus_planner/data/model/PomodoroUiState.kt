package com.example.focus_planner.data.model

data class PomodoroUiState(
    val isRunning: Boolean = false,
    val timeLeft: Int = 25 * 60,
    val workTime: Int = 25,
    val breakTime: Int = 5,
    val isWorkTime: Boolean = true
)
