package com.example.focus_planner.ui.calendar

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import org.threeten.bp.LocalDate // Cambia la importación

// Modelo de ejemplo para eventos en el calendario
data class CalendarTask(val date: LocalDate, val title: String)

// Lista de tareas de ejemplo organizadas por fecha
val sampleCalendarTasks = listOf(
    CalendarTask(LocalDate.of(2025, 4, 5), "Entrega del informe"),
    CalendarTask(LocalDate.of(2025, 4, 6), "Revisión de código"),
    CalendarTask(LocalDate.of(2025, 4, 7), "Reunión con el equipo")
)

@Composable
fun CalendarScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = "Calendario de Tareas", style = MaterialTheme.typography.headlineLarge)

        LazyColumn(modifier = Modifier.padding(top = 16.dp)) {
            items(sampleCalendarTasks) { task ->
                CalendarTaskCard(task)
            }
        }
    }
}

@Composable
fun CalendarTaskCard(task: CalendarTask) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = task.date.toString(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimary)
            Text(text = task.title, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onPrimary)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewCalendarScreen() {
    CalendarScreen()
}
