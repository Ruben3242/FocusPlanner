package com.example.focus_planner.data.model

import androidx.annotation.RawRes
import com.example.focus_planner.R

data class OnboardingPage(
    val title: String,
    val description: String,
    @RawRes val animationRes: Int
)

val onboardingPages = listOf(
    OnboardingPage(
        title = "Organiza tu día",
        description = "Crea, edita y completa tareas fácilmente",
        animationRes = R.raw.task_animation
    ),
    OnboardingPage(
        title = "Enfócate con Pomodoro",
        description = "Mejora tu productividad con sesiones cronometradas",
        animationRes = R.raw.pomodoro_animation1
    ),
    OnboardingPage(
        title = "Todo bajo control",
        description = "Consulta estadísticas y mantente motivado",
        animationRes = R.raw.productivity_animation2
    )
)