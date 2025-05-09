package com.example.focus_planner.ui.screens.tasks

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.focus_planner.viewmodel.TaskViewModel
import java.util.*
import android.app.DatePickerDialog
import com.example.focus_planner.utils.SharedPreferencesManager
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Locale


@Composable
fun TaskDetailScreen(
    navController: NavController,
    taskId: String?,
    viewModel: TaskViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val task by viewModel.taskDetail.collectAsState()
    val calendar = remember { Calendar.getInstance() }
    var taskState by remember { mutableStateOf(task) }


    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    fun showDatePicker(onDateSelected: (String) -> Unit) {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                val formattedDate = dateFormat.format(calendar.time)
                onDateSelected(formattedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    // Cargar tarea si no está cargada
    LaunchedEffect(taskId) {
        taskId?.toLongOrNull()?.let {
            viewModel.loadTaskDetail(it, context)
        }
    }

    task?.let { task ->
        val userId = SharedPreferencesManager.getUserId(context)
        Log.d("TaskDetailScreen", "Tarea cargada: ${task.title} - ${task.description} - ${task.dueDate} - ${task.completed} - ${task.googleCalendarEventId} - ${task.status} - ${task.priority} - $userId")
        Spacer(modifier = Modifier.height(40.dp))
        Card(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),

            elevation = CardDefaults.cardElevation(6.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DateRange, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Vence: ${formatDate(task.dueDate)}")
                }

                Spacer(modifier = Modifier.height(8.dp))

                val timeRemainingInfo = calculateTimeRemaining(task.dueDate)

                val animatedAlpha by animateFloatAsState(
                    targetValue = if (timeRemainingInfo.urgency >= 1) 0.5f else 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "TimeRemainingAlpha"
                )

                Text(
                    text = "Tiempo restante: ${timeRemainingInfo.text}",
                    color = when (timeRemainingInfo.urgency) {
                        2 -> Color.Red
                        1 -> Color(0xFFFF9800) // Naranja
                        else -> Color.Gray
                    },
                    fontWeight = if (timeRemainingInfo.urgency >= 1) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier.alpha(animatedAlpha)
                )
                Spacer(modifier = Modifier.height(24.dp))

                // Botón completar
                if (!task.completed) {
                    Button(
                        onClick = { viewModel.markTaskAsCompleted(context) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Marcar como completada")
                    }
                } else {
                    Text(
                        text = "¡Tarea completada!",
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))


                if (timeRemainingInfo.text == "Expirada") {
                    Button(
                        onClick = {
                            showDatePicker { newDate ->
                                viewModel.updateTaskDueDate(task.id, newDate, context)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107)) // amarillo llamativo
                    ) {
                        Text("Cambiar fecha de vencimiento")
                    }
                }



                Spacer(modifier = Modifier.height(16.dp))

                // Enlace a Google Calendar
                if (!task.googleCalendarEventId.isNullOrEmpty()) {
                    OutlinedButton(
                        onClick = {
                            val url =
                                "https://calendar.google.com/calendar/event?eid=${task.googleCalendarEventId}"
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Abrir en Google Calendar",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Ver en Google Calendar")
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Botón volver
                OutlinedButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Volver")
                }

//// Si está expirada, mostrar botón para cambiar fecha
//                if (timeRemainingInfo.text == "Expirada") {
//                    Spacer(modifier = Modifier.height(8.dp))
//                    OutlinedButton(
//                        onClick = {
//                            // Aquí podrías abrir un DatePickerDialog para cambiar la fecha
//                            // Por ahora solo simula la acción
//                            // TODO: implementar lógica para seleccionar nueva fecha
//                        },
//                        modifier = Modifier.fillMaxWidth()
//                    ) {
//                        Text("Cambiar fecha de vencimiento")
//                    }
//                }

                val taskDeleted by viewModel.taskDeleted.collectAsState()
                LaunchedEffect(taskDeleted) {
                    if (taskDeleted == true) {
                        navController.navigate("tasks") {
                            popUpTo("tasks") { inclusive = true }
                        }
                        viewModel.resetTaskDeleted() // esta función la crearemos en el paso 2
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        viewModel.deleteTask(context, task.id)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Text("Eliminar tarea", color = Color.White)
                }

            }
        }

    }
}

private fun formatDate(dueDate: String?): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val date = inputFormat.parse(dueDate ?: "") ?: return "Sin fecha"
        outputFormat.format(date)
    } catch (e: Exception) {
        "Fecha inválida"
    }
}


data class TimeRemainingInfo(val text: String, val urgency: Int) // 0 = normal, 1 = <1h, 2 = <30min

private fun calculateTimeRemaining(dueDate: String?): TimeRemainingInfo {
    if (dueDate == null) return TimeRemainingInfo("Sin fecha", 0)
    return try {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val due = formatter.parse(dueDate)?.time ?: return TimeRemainingInfo("Formato inválido", 0)

        // Como no hay hora, asumimos medianoche
        val dueWithTime = due + (24 * 60 * 60 * 1000) - 1 // Último milisegundo del día

        val now = System.currentTimeMillis()
        val remaining = dueWithTime - now

        if (remaining <= 0) {
            TimeRemainingInfo("Expirada", 2)
        } else {
            val days = remaining / (1000 * 60 * 60 * 24)
            val hours = remaining / (1000 * 60 * 60)
            val minutes = (remaining / (1000 * 60)) % 60
            val urgency = when {
                remaining <= 30 * 60 * 1000 -> 2
                remaining <= 60 * 60 * 1000 -> 1
                else -> 0
            }
            TimeRemainingInfo(
                text = if (days > 0) {
                    "$days días"
                } else if (hours > 0) {
                    "$hours horas y $minutes minutos"
                } else {
                    "$minutes minutos"
                },
                urgency = urgency
            )
        }
    } catch (e: Exception) {
        TimeRemainingInfo("Error de fecha", 0)
    }
}

