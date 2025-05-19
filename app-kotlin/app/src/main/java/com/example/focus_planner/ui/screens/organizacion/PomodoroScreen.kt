package com.example.focus_planner.ui.screens.organizacion

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun PomodoroTopBar(
    navController: NavController,
    isOnBreak: Boolean
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
            text = if (isOnBreak) "RECESO" else "POMODORO",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onPrimary
        )
        IconButton(onClick = { navController.navigate("home") }) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Cerrar",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
fun PomodoroScreen(
    navController: NavController
) {
    var isTimerRunning by remember { mutableStateOf(false) }
    var isOnBreak by remember { mutableStateOf(false) }
    var timeLeft by remember { mutableStateOf(25 * 60) } // 25 minutos de trabajo
    var workTime by remember { mutableStateOf(25) } // Tiempo de trabajo configurado
    var breakTime by remember { mutableStateOf(5) } // Tiempo de descanso configurado

    val startTimer = { if (!isTimerRunning) isTimerRunning = true }
    val pauseTimer = { if (isTimerRunning) isTimerRunning = false }
    val resetTimer = {
        timeLeft = workTime * 60
        isTimerRunning = false
        isOnBreak = false
    }

    var isWorkTime by remember { mutableStateOf(true) }

//    var workTime by remember { mutableStateOf(25) } // minutos
//    var breakTime by remember { mutableStateOf(5) } // minutos
//    var timeLeft by remember { mutableStateOf(workTime * 60) } // en segundos

    // Sincroniza timeLeft con el nuevo valor de workTime si el temporizador no está corriendo
    LaunchedEffect(workTime, isTimerRunning, isWorkTime) {
        if (!isTimerRunning && isWorkTime) {
            timeLeft = workTime * 60
        }
    }

    // Sincroniza también para breakTime si lo usas (opcional)
    LaunchedEffect(breakTime, isTimerRunning, isWorkTime) {
        if (!isTimerRunning && !isWorkTime) {
            timeLeft = breakTime * 60
        }
    }

    // Lógica del temporizador (reducida aquí)
    LaunchedEffect(isTimerRunning, timeLeft) {
        if (isTimerRunning && timeLeft > 0) {
            delay(1000L)
            timeLeft -= 1
        } else if (isTimerRunning && timeLeft == 0) {
            isTimerRunning = false
            isWorkTime = !isWorkTime
            timeLeft = if (isWorkTime) workTime * 60 else breakTime * 60
        }
    }

    Scaffold(
        topBar = {
            PomodoroTopBar(navController = navController, isOnBreak = isOnBreak)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
//            Text(
//                text = if (isOnBreak) "RECESO" else "POMODORO",
//                style = MaterialTheme.typography.headlineMedium,
//                color = MaterialTheme.colorScheme.primary,
//                modifier = Modifier.padding(bottom = 20.dp)
//            )

            // Texto con tiempo
            Text(
                text = "${timeLeft / 60}:${String.format("%02d", timeLeft % 60)}",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0D6EFD)
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            AnimatedProgressCircle(
                timeLeft = timeLeft,
                totalTime = if (isOnBreak) breakTime * 60 else workTime * 60,
                isRunning = isTimerRunning,
                onToggle = { isTimerRunning = !isTimerRunning }
            )

            Spacer(modifier = Modifier.height(30.dp))

//            Row(
//                horizontalArrangement = Arrangement.Center,
//                modifier = Modifier.fillMaxWidth()
//            ) {
//                Button(
//                    onClick = { if (isTimerRunning) pauseTimer() else startTimer() },
//                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
//                    shape = MaterialTheme.shapes.medium,
//                    modifier = Modifier.weight(1f)
//                ) {
//                    Text(text = if (isTimerRunning) "Pausar" else "Iniciar", color = MaterialTheme.colorScheme.onPrimary)
//                }
//
//                Spacer(modifier = Modifier.width(16.dp))
//
//                Button(
//                    onClick = resetTimer,
//                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
//                    shape = MaterialTheme.shapes.medium,
//                    modifier = Modifier.weight(1f)
//                ) {
//                    Text("Reiniciar")
//                }
//            }

            Spacer(modifier = Modifier.height(30.dp))
            Divider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
            Spacer(modifier = Modifier.height(30.dp))

            Text(
                text = "Tiempo de trabajo: $workTime minutos",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Slider(
                value = workTime.toFloat(),
                onValueChange = { if (!isTimerRunning) workTime = it.toInt() }, // Opcional para reforzar
                valueRange = 10f..60f,
                steps = 50,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isTimerRunning // Deshabilita el slider si el timer está activo
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Tiempo de descanso: $breakTime minutos",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Slider(
                value = breakTime.toFloat(),
                onValueChange = { if (!isTimerRunning) breakTime = it.toInt() },
                valueRange = 1f..30f,
                steps = 29,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isTimerRunning
            )

        }
    }
}



@Composable
fun AnimatedProgressCircle(
    timeLeft: Int,
    totalTime: Int,
    isRunning: Boolean,
    onToggle: () -> Unit
) {
    val progress = timeLeft / totalTime.toFloat()
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000)
    )
    val sweepAngle = animatedProgress * 360f

    val backgroundColor = Color(0xFF121212) // Negro muy oscuro
    val trackColor = Color(0xFF2C2F33)      // Gris oscuro para la pista
    val progressGradient = Brush.sweepGradient(
        colors = listOf(
            Color(0xFF0D6EFD),   // Azul brillante
            Color(0xFF0056B3)    // Azul oscuro
        )
    )

    Box(
        modifier = Modifier
            .size(220.dp)
            .clickable { onToggle() },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(220.dp)) {
            drawCircle(
                color = Color.Black.copy(alpha = 0.5f),
                radius = size.minDimension / 2,
                center = center,
                style = Stroke(width = 22.dp.toPx(), cap = StrokeCap.Round)
            )

            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = 18.dp.toPx(), cap = StrokeCap.Round)
            )

            drawArc(
                brush = progressGradient,
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = 18.dp.toPx(), cap = StrokeCap.Round)
            )

            drawCircle(
                color = backgroundColor,
                radius = size.minDimension / 2 - 18.dp.toPx() - 10.dp.toPx(),
                center = center
            )
        }


        // Icono de play o pausa sobre el texto, con fondo circular para mejor legibilidad
        Icon(
            imageVector = if (isRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow,
            contentDescription = if (isRunning) "Pausar" else "Reanudar",
            tint = Color(0xFF0D6EFD),
            modifier = Modifier
                .size(48.dp)
                .align(Alignment.Center)
                .background(
                    color = Color.Black.copy(alpha = 0.5f),
                    shape = MaterialTheme.shapes.small
                )
                .padding(8.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PomodoroScreenPreview() {
    PomodoroScreen(navController = NavController(context = LocalContext.current))
}