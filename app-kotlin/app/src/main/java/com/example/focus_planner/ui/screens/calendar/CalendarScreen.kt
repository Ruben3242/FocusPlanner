package com.example.focus_planner.ui.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import com.example.focus_planner.data.model.task.Task
import com.example.focus_planner.data.model.task.TaskPriority
import com.example.focus_planner.ui.components.TopBarWithClose
import com.example.focus_planner.ui.screens.home.AccentColor
import com.example.focus_planner.ui.screens.home.BackgroundColor
import com.example.focus_planner.ui.screens.home.CardColor
import com.example.focus_planner.ui.screens.home.TextPrimary
import com.example.focus_planner.ui.screens.home.TextSecondary
import com.example.focus_planner.utils.SharedPreferencesManager.getToken
import com.example.focus_planner.utils.TokenManager
import com.example.focus_planner.viewmodel.CalendarViewModel
import org.threeten.bp.*
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.TextStyle
import java.util.*

data class CalendarTask(val date: LocalDate, val title: String)

@Composable
fun CalendarScreen(viewModel: CalendarViewModel, navController: NavController) {
    val context = LocalContext.current
    val tasks by viewModel.tasks.collectAsState()
    val currentMonth by viewModel.currentYearMonth.collectAsState()

//    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }

    val today = LocalDate.now()
    val daysInMonth = currentMonth.lengthOfMonth()
    val firstDayOfMonth = currentMonth.atDay(1)
    val startOffset = (firstDayOfMonth.dayOfWeek.value + 6) % 7 // Empieza en lunes

    val days = (1..daysInMonth).map { currentMonth.atDay(it) }

    val token = getToken(context)


    LaunchedEffect(true) {
        if (token != null) {
            viewModel.cargarToken(token)
        }
    }

    Scaffold(
        topBar = {
            CalendarTopBar(
                navController = navController
            )
        }, modifier = Modifier
            .fillMaxSize()
            .background(color = BackgroundColor)

            ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF121212)).padding(padding)
                .padding(16.dp)
        ) {
            Header(
                month = currentMonth,
                onPreviousMonth = {
                    if (token != null) {
                        viewModel.onMonthChanged(currentMonth.minusMonths(1), token)
                    }
                },
                onNextMonth = {
                    if (token != null) {
                        viewModel.onMonthChanged(currentMonth.plusMonths(1), token)
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            DayOfWeekHeader()

            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(4.dp)
            ) {
                repeat(startOffset) {
                    item { Box(modifier = Modifier.size(48.dp)) }
                }

                items(days) { date ->
                    val dayTasks = tasks.filter { it.date == date }
                    val isToday = date == today
                    val isPast = date.isBefore(today)

                    DayCell(
                        date = date,
                        isToday = isToday,
                        hasTask = dayTasks.isNotEmpty(),
                        isExpired = isPast && dayTasks.isNotEmpty(),
                        taskCount = dayTasks.size,
                        onClick = { selectedDate = date }
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                LegendItem(color = Color(0xFFE53935), label = "Expirada")
                LegendItem(color = Color(0xFF43A047), label = "Pendiente")
                LegendItem(color = Color(0xFF1E88E5), label = "Hoy")
                LegendItem(color = Color.Black, label = "Múltiples tareas", hasBorder = true)
            }


            selectedDate?.let { date ->
                val dayTasks = tasks.filter { it.date == date }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Tareas para ${date.dayOfMonth}/${date.monthValue}/${date.year}",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium
                )
                if (dayTasks.isNotEmpty()) {
                    Column(modifier = Modifier.padding(top = 8.dp)) {
                        dayTasks.forEach {
                            Text(
                                text = "• ${it.title}",
                                color = Color.LightGray,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                } else {
                    Text(
                        text = "No hay tareas para este día.",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String, hasBorder: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .then(
                    if (hasBorder) Modifier.border(2.dp, Color.White, CircleShape) else Modifier
                )
                .background(color = color, shape = CircleShape)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = label, color = Color.White, fontSize = 12.sp)
    }
}


@Composable
fun Header(month: YearMonth, onPreviousMonth: () -> Unit, onNextMonth: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousMonth) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Anterior", tint = Color.White)
        }
        Text(
            text = "${month.month.getDisplayName(TextStyle.FULL, Locale("es"))} ${month.year}",
            style = MaterialTheme.typography.titleLarge,
            color = Color.White
        )
        IconButton(onClick = onNextMonth) {
            Icon(Icons.Default.ArrowForward, contentDescription = "Siguiente", tint = Color.White)
        }
    }
}

@Composable
fun DayOfWeekHeader() {
    val daysOfWeek = listOf(
        DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY
    )
    Row(modifier = Modifier.fillMaxWidth()) {
        for (day in daysOfWeek) {
            Text(
                text = day.getDisplayName(TextStyle.SHORT, Locale("es")),
                modifier = Modifier
                    .weight(1f)
                    .padding(4.dp),
                textAlign = TextAlign.Center,
                color = Color.LightGray,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun DayCell(
    date: LocalDate,
    isToday: Boolean,
    hasTask: Boolean,
    isExpired: Boolean,
    taskCount: Int,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isToday -> Color(0xFF1E88E5)
        isExpired -> Color(0xFFE53935)
        hasTask -> Color(0xFF43A047)
        else -> Color(0xFF2C2C2C)
    }

    val textColor = if (hasTask || isToday || isExpired) Color.White else Color.LightGray

    val borderModifier = if (taskCount > 1) {
        Modifier.border(
            width = 2.dp,
            color = Color.White,
            shape = MaterialTheme.shapes.small
        )
    } else Modifier

    Box(
        modifier = Modifier
            .padding(4.dp)
            .size(48.dp)
            .then(borderModifier)
            .background(backgroundColor, shape = MaterialTheme.shapes.small)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            color = textColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun CalendarLegend() {
    Column(modifier = Modifier.padding(top = 16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(12.dp).background(Color(0xFF43A047)))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Día con tareas pendientes", color = Color.White)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(12.dp).background(Color(0xFF1E88E5)))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Hoy", color = Color.White)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(12.dp).background(Color(0xFFE53935)))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Tareas expiradas", color = Color.White)
        }
    }
}

@Composable
fun CalendarTopBar(
    navController: NavController
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
            text = "Calendario de tareas",
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
