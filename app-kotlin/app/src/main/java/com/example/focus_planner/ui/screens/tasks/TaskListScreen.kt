package com.example.focus_planner.ui.screens.tasks

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.example.focus_planner.utils.TokenManager

// Modelo temporal para tareas
data class Task(val id: Int, val title: String, val description: String)

// Lista de ejemplo
val sampleTasks = listOf(
    Task(1, "Hacer el informe", "Completar el informe mensual de la empresa."),
    Task(2, "Revisar código", "Verificar la implementación en el módulo de autenticación."),
    Task(3, "Reunión con el equipo", "Discutir los próximos pasos del proyecto.")
)

@Composable
fun TaskListScreen(onTaskClick: (Int) -> Unit) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        TokenManager.checkTokenAndRefresh(context, NavController(context))
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Mis Tareas",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn {
            items(sampleTasks) { task ->
                TaskCard(task, onTaskClick)
            }
        }
    }
}

@Composable
fun TaskCard(task: Task, onTaskClick: (Int) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onTaskClick(task.id) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF03DAC5))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = task.title, style = MaterialTheme.typography.bodyLarge, color = Color.Black)
            Text(text = task.description, style = MaterialTheme.typography.bodyMedium, color = Color.DarkGray)
        }
    }
}

//@Preview(showBackground = true)
//@Composable
//fun PreviewTaskListScreen() {
//    TaskListScreen(onTaskClick = {}, navController = NavController(LocalContext.current)) // Proporciona un NavController de prueba
//}
