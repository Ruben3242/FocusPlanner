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
import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Close
import com.example.focus_planner.utils.SharedPreferencesManager
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun TopBarWithClose(
    title: String,
    onCloseClick: () -> Unit
) {
    val barHeight = 48.dp
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(barHeight)
            .background(MaterialTheme.colorScheme.primary)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onPrimary
        )

        IconButton(onClick = onCloseClick) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Cerrar",
                tint = Color.White
            )
        }
    }
}

@Composable
fun TaskDetailScreen(
    navController: NavController,
    taskId: String?,
    viewModel: TaskViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val task by viewModel.taskDetail.collectAsState()
    val calendar = remember { Calendar.getInstance() }

    LaunchedEffect(taskId) {
        taskId?.toLongOrNull()?.let { viewModel.loadTaskDetail(it, context) }
    }

    Scaffold(
        topBar = {
            TopBarWithClose(
                title = "Detalle de tarea",
                onCloseClick = { navController.popBackStack() }
            )
        },
        containerColor = Color(0xFFF5F5F5) // Fondo claro
    ) { padding ->
        task?.let { t ->
            val timeInfo = remember(t.dueDate) { calculateTimeRemaining(t.dueDate) }
            val titleColor = if (t.completed) Color(0xFF2E7D32) else Color(0xFF212121)

            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(6.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFFFF)) // blanco
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Text(
                            text = t.title,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = titleColor
                        )

                        Spacer(Modifier.height(8.dp))

                        Text(
                            text = t.description,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(0xFF616161) // gris oscuro
                        )

                        Spacer(Modifier.height(16.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DateRange, null, tint = Color(0xFF1976D2))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Vence: ${formatDate(t.dueDate)}",
                                color = Color(0xFF424242)
                            )
                        }

                        Spacer(Modifier.height(8.dp))
                        val urgencyColor = when (timeInfo.urgency) {
                            2 -> Color(0xFFD32F2F) // rojo
                            1 -> Color(0xFFFFA000) // ámbar
                            else -> Color(0xFF757575) // gris
                        }

                        Text(
                            text = "Tiempo restante: ${timeInfo.text}",
                            color = urgencyColor,
                            fontWeight = if (timeInfo.urgency > 0) FontWeight.Bold else FontWeight.Normal
                        )

                        Spacer(Modifier.height(24.dp))
                        if (!t.completed) {
                            Button(
                                onClick = { viewModel.markTaskAsCompleted(context) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF66BB6A))
                            ) { Text("Marcar como completada") }
                        } else {
                            Text(
                                text = "¡Tarea completada!",
                                color = Color(0xFF388E3C),
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        if (timeInfo.text == "Expirada") {
                            Spacer(Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    showDatePicker(context, calendar) { newDate ->
                                        viewModel.updateTaskDueDate(t.id, newDate, context)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD54F))
                            ) { Text("Cambiar fecha de vencimiento") }
                        }

                        if (!t.googleCalendarEventId.isNullOrEmpty()) {
                            Spacer(Modifier.height(16.dp))
                            OutlinedButton(
                                onClick = {
                                    val url = "https://calendar.google.com/calendar/event?eid=${t.googleCalendarEventId}"
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                },
                                modifier = Modifier.fillMaxWidth(),
                                border = BorderStroke(1.dp, Color(0xFF1565C0))
                            ) {
                                Icon(Icons.Default.DateRange, null, tint = Color(0xFF1565C0))
                                Spacer(Modifier.width(8.dp))
                                Text("Ver en Google Calendar", color = Color(0xFF1565C0))
                            }
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                OutlinedButton(
                    onClick = { navController.navigate("editTask/${t.id}") },
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, Color.Gray),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF212121))
                ) { Text("Editar") }

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = { viewModel.deleteTask(context, t.id) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                ) { Text("Eliminar tarea", color = Color.White) }

                val deleted by viewModel.taskDeleted.collectAsState()
                LaunchedEffect(deleted) {
                    if (deleted == true) {
                        navController.navigate("tasks") { popUpTo("tasks") { inclusive = true } }
                        viewModel.resetTaskDeleted()
                    }
                }
            }
        }
    }
}


/* util: date-picker */
private fun showDatePicker(
    context: Context,
    calendar: Calendar,
    onDateSelected: (String) -> Unit
) {
    val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    DatePickerDialog(
        context,
        { _, y, m, d ->
            calendar.set(y, m, d)
            onDateSelected(format.format(calendar.time))
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).show()
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

