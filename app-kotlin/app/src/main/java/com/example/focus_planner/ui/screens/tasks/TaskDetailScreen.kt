package com.example.focus_planner.ui.screens.tasks

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.focus_planner.data.model.Task
import com.example.focus_planner.data.model.TaskPriority
import com.example.focus_planner.data.model.TaskStatus
import com.example.focus_planner.viewmodel.TaskViewModel

// Aquí definimos una lista de tareas simuladas para los detalles de la tarea
@Composable
fun TaskDetailScreen(navController: NavController, taskId: String?, token: String) {
    val context = LocalContext.current
    val taskViewModel: TaskViewModel = hiltViewModel()

    // Observamos el estado de la tarea
    val task by taskViewModel.selectedTask.collectAsState()

    // Cargar los detalles de la tarea cuando cambia el taskId
    LaunchedEffect(taskId) {
        taskId?.toLongOrNull()?.let { id ->
            taskViewModel.loadTaskDetails(id, token)
        }
    }


    // Si la tarea aún no está cargada, mostramos un indicador de carga
    if (task == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Título de la tarea
            Text(
                text = task!!.title,
                style = MaterialTheme.typography.headlineLarge,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Descripción de la tarea
            Text(
                text = task!!.description,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Fecha de vencimiento
            Text(
                text = "Vence el: ${task!!.dueDate}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Botón para volver a la lista de tareas
            Button(onClick = { navController.popBackStack() }) {
                Text(text = "Volver")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewTaskDetailScreen() {
    TaskDetailScreen(navController = NavController(LocalContext.current), taskId = "1", token = "token_de_prueba")
}