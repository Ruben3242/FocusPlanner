package com.example.focus_planner.ui.screens.organizacion

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.focus_planner.ui.notificaciones.PomodoroActionReceiver
import com.example.focus_planner.ui.notificaciones.showPomodoroNotification
import com.example.focus_planner.ui.notificaciones.updatePomodoroProgressNotification
import com.example.focus_planner.utils.SharedPreferencesManager.loadPomodoroState
import com.example.focus_planner.utils.SharedPreferencesManager.savePomodoroState
import com.example.focus_planner.viewmodel.PomodoroViewModel

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
    navController: NavController,
    viewModel: PomodoroViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    // Observando estado del ViewModel
    val isTimerRunning by viewModel.isTimerRunning.collectAsState()
    val timeLeft by viewModel.timeLeft.collectAsState()
    val workTime by viewModel.workTime.collectAsState()
    val breakTime by viewModel.breakTime.collectAsState()
    val isWorkTime by viewModel.isWorkTime.collectAsState()

    var workTimeInput by remember { mutableStateOf(workTime.toFloat()) }
    var breakTimeInput by remember { mutableStateOf(breakTime.toFloat()) }

    var showWorkDialog by remember { mutableStateOf(false) }
    var showBreakDialog by remember { mutableStateOf(false) }

    // Registrar BroadcastReceiver
    DisposableEffect(Unit) {
        val internalReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.getStringExtra("ACTION")) {
                    "ACTION_PAUSE_TIMER" -> viewModel.toggleTimer()
                    "ACTION_RESUME_TIMER" -> viewModel.toggleTimer()
                }
            }
        }

        val filter = IntentFilter("POMODORO_TIMER_ACTION")
        ContextCompat.registerReceiver(
            context,
            internalReceiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        onDispose {
            context.unregisterReceiver(internalReceiver)
        }
    }


    Scaffold(
        topBar = {
            PomodoroTopBar(navController = navController, isOnBreak = !isWorkTime)
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
                totalTime = if (isWorkTime) workTime * 60 else breakTime * 60,
                isRunning = isTimerRunning,
                onToggle = { viewModel.toggleTimer() }
            )

            Spacer(modifier = Modifier.height(30.dp))
            Divider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
            Spacer(modifier = Modifier.height(30.dp))

            Text(
                text = "Tiempo de trabajo: ${workTimeInput.toInt()} minutos",
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !isTimerRunning) {
                        showWorkDialog = true
                    }
            )

            Slider(
                value = workTimeInput,
                onValueChange = { workTimeInput = it },
                valueRange = 10f..60f,
                steps = 50,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isTimerRunning
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Tiempo de descanso: ${breakTimeInput.toInt()} minutos",
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !isTimerRunning) {
                        showBreakDialog = true
                    }
            )

            Slider(
                value = breakTimeInput,
                onValueChange = { breakTimeInput = it },
                valueRange = 1f..30f,
                steps = 29,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isTimerRunning
            )



            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    viewModel.setWorkAndBreakTimes(
                        workTimeInput.toInt(),
                        breakTimeInput.toInt()
                    )
                },
                enabled = !isTimerRunning,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Establecer tiempos")
            }
            if (showWorkDialog) {
                TimeInputDialog(
                    title = "Establecer tiempo de trabajo",
                    initialValue = workTimeInput.toInt(),
                    valueRange = 25..60,
                    onDismiss = { showWorkDialog = false },
                    onConfirm = {
                        workTimeInput = it.toFloat()
                    }
                )
            }

            if (showBreakDialog) {
                TimeInputDialog(
                    title = "Establecer tiempo de descanso",
                    initialValue = breakTimeInput.toInt(),
                    valueRange = 1..30,
                    onDismiss = { showBreakDialog = false },
                    onConfirm = {
                        breakTimeInput = it.toFloat()
                    }
                )
            }

        }
    }
}
@Composable
fun TimeInputDialog(
    title: String,
    initialValue: Int,
    valueRange: IntRange,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var input by remember { mutableStateOf(initialValue.toString()) }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = input,
                onValueChange = {
                    input = it.filter { char -> char.isDigit() }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                label = { Text("Minutos") }
            )
        },
        confirmButton = {
            IconButton(onClick = {
                input.toIntOrNull()?.let {
                    if (it in valueRange) {
                        onConfirm(it)
                    }
                }
                onDismiss()
            }) {
                Icon(Icons.Default.Check, contentDescription = "Confirmar")
            }
        },
        dismissButton = {
            IconButton(onClick = { onDismiss() }) {
                Icon(Icons.Default.Close, contentDescription = "Cancelar")
            }
        }
    )
}

@Composable
fun EditableTimeSetting(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    enabled: Boolean,
    onValueChange: (Float) -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    var textValue by remember { mutableStateOf(value.toInt().toString()) }

    val focusManager = LocalFocusManager.current

    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable(enabled = enabled) { isEditing = true }
        ) {
            if (isEditing) {
                OutlinedTextField(
                    value = textValue,
                    onValueChange = {
                        textValue = it.filter { char -> char.isDigit() }
                        textValue.toIntOrNull()?.let { newValue ->
                            if (newValue in range.start.toInt()..range.endInclusive.toInt()) {
                                onValueChange(newValue.toFloat())
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .width(100.dp)
                        .onFocusChanged { focusState ->
                            if (!focusState.isFocused) {
                                isEditing = false
                                textValue.toIntOrNull()?.let { newValue ->
                                    if (newValue in range.start.toInt()..range.endInclusive.toInt()) {
                                        onValueChange(newValue.toFloat())
                                    }
                                }
                            }
                        },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                        }
                    ),
                    label = { Text(label) },
                    trailingIcon = {
                        IconButton(onClick = {
                            isEditing = false
                            focusManager.clearFocus()
                        }) {
                            Icon(Icons.Default.Check, contentDescription = "Confirmar")
                        }
                    }
                )
            } else {
                Text(
                    text = "$label: ${value.toInt()} minutos",
                    modifier = Modifier.padding(vertical = 4.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        Slider(
            value = value,
            onValueChange = {
                onValueChange(it)
                textValue = it.toInt().toString()
            },
            valueRange = range,
            steps = (range.endInclusive - range.start).toInt() - 1,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled
        )
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