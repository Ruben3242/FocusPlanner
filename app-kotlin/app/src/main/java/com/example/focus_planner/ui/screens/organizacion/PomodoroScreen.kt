package com.example.focus_planner.ui.screens.organizacion

import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.focus_planner.data.model.UpdateUserRequest
import com.example.focus_planner.data.model.User
import com.example.focus_planner.utils.SharedPreferencesManager
import com.example.focus_planner.utils.TokenManager
import com.example.focus_planner.viewmodel.ProfileViewModel
import kotlinx.coroutines.delay

@Composable
fun PomodoroScreen(
    navController: NavController
) {
    var isTimerRunning by remember { mutableStateOf(false) }
    var isOnBreak by remember { mutableStateOf(false) }
    var timeLeft by remember { mutableStateOf(25 * 60) } // 25 minutos de trabajo
    var timeToRest by remember { mutableStateOf(5 * 60) } // 5 minutos de descanso
    var workTime by remember { mutableStateOf(25) } // Tiempo de trabajo configurado por el usuario
    var breakTime by remember { mutableStateOf(5) } // Tiempo de descanso configurado por el usuario

    // Función para iniciar el temporizador
    val startTimer: () -> Unit = {
        if (!isTimerRunning) {
            isTimerRunning = true
        }
    }

    // Función para pausar el temporizador
    val pauseTimer: () -> Unit = {
        if (isTimerRunning) {
            isTimerRunning = false
        }
    }

    // Función para reiniciar el temporizador
    val resetTimer: () -> Unit = {
        timeLeft = workTime * 60 // Resetear a los minutos de trabajo configurados
        isTimerRunning = false
        isOnBreak = false
    }

    // Lógica para la cuenta atrás
    LaunchedEffect(isTimerRunning) {
        if (isTimerRunning) {
            while (timeLeft > 0 && isTimerRunning) {
                delay(1000)  // Espera un segundo
                timeLeft -= 1
            }
            if (timeLeft == 0 && !isOnBreak) {
                isOnBreak = true
                timeLeft = breakTime * 60 // Activar el descanso
            }
        }
    }
    Column {
        // Botón de volver
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cerrar",
                    tint = colorScheme.primary
                )
            }
        }


    }

    // Diseño
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (isOnBreak) "RECESO" else "POMODORO",
            style = MaterialTheme.typography.titleLarge,
            color = colorScheme.primary,
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Mostrar el contador de tiempo restante
        Text(
            text = "${timeLeft / 60}:${String.format("%02d", timeLeft % 60)}",
            style = MaterialTheme.typography.displayLarge,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Círculo de Progreso
        AnimatedProgressCircle(timeLeft = timeLeft, totalTime = if (isOnBreak) breakTime else workTime)

        Spacer(modifier = Modifier.height(20.dp))

        // Botones de Control
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = {
                    if (isTimerRunning) pauseTimer() else startTimer()
                }
            ) {
                Text(text = if (isTimerRunning) "Pausar" else "Iniciar")
            }

            Spacer(modifier = Modifier.width(16.dp))

            Button(onClick = { resetTimer() }) {
                Text("Reiniciar")
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Divider(color = Color.Gray, thickness = 1.dp)
        // Personalización de tiempos de trabajo y descanso
        Spacer(modifier = Modifier.height(20.dp))
        Text("Tiempo de trabajo: $workTime minutos")
        Slider(
            value = workTime.toFloat(),
            onValueChange = { workTime = it.toInt() },
            valueRange = 10f..60f,
            steps = 50,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text("Tiempo de descanso: $breakTime minutos")
        Slider(
            value = breakTime.toFloat(),
            onValueChange = { breakTime = it.toInt() },
            valueRange = 1f..30f,
            steps = 29,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
@Composable
fun ProgressCircle(timeLeft: Int, totalTime: Int) {
    val progress = (timeLeft / totalTime.toFloat()) * 360f
    val animatedProgress by animateFloatAsState(targetValue = progress, animationSpec = tween(durationMillis = 1000))
    val primaryColor = colorScheme.primary
    Canvas(modifier = Modifier.size(200.dp)) {
        drawArc(
            color = primaryColor,
            startAngle = -90f,
            sweepAngle = animatedProgress,
            useCenter = false,
            size = size,
            style = Stroke(width = 10.dp.toPx())
        )
    }
}

@Composable
fun AnimatedProgressCircle(timeLeft: Int, totalTime: Int) {
    val progress = (timeLeft / totalTime.toFloat()) * 360f
    val animatedProgress by animateFloatAsState(targetValue = progress, animationSpec = tween(durationMillis = 1000))

    val primaryColor = colorScheme.primary
    Canvas(modifier = Modifier.size(200.dp)) {
        drawArc(
            color = primaryColor,
            startAngle = -90f,
            sweepAngle = animatedProgress,
            useCenter = false,
            size = size,
            style = Stroke(width = 10.dp.toPx())
        )
    }
}
