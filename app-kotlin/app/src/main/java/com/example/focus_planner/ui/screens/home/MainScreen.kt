package com.example.focus_planner.ui.screens.home

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddToPhotos
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.focus_planner.utils.SharedPreferencesManager
import com.example.focus_planner.utils.SharedPreferencesManager.clearSession
import com.example.focus_planner.utils.TokenManager
import com.example.focus_planner.viewmodel.ProfileViewModel
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.charts.RadarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.data.RadarData
import com.github.mikephil.charting.data.RadarDataSet
import com.github.mikephil.charting.data.RadarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import kotlinx.coroutines.delay

// Paleta colores
val BackgroundColor = Color.White // Gris oscuro más claro que antes
val CardColor = Color.White // Gris carbón suave para cartas
val TextPrimary = Color.Black
val TextSecondary = Color(0xFFB0B0B0)
val AccentColor = Color(0xFF1565C0) // Azul suave para acentos

@Composable
fun MainScreen(
    onNavigate: (String) -> Unit,
    navController: NavController,
    token: String
) {
    val context = LocalContext.current
    val viewModel: ProfileViewModel = hiltViewModel()
    val user by viewModel.userProfile.collectAsState(initial = null)

    val totalTasks by viewModel.totalTasks.collectAsState()
    val completedTasks by viewModel.completedTasks.collectAsState()
    val mostProductiveHour by viewModel.mostProductiveHour.collectAsState()
    val showChart = remember { mutableStateOf(false) }
    val sharedPrefs =
        LocalContext.current.getSharedPreferences("focus_planner_prefs", Context.MODE_PRIVATE)

    LaunchedEffect(Unit) {
        TokenManager.checkTokenAndRefresh(context, navController)
        viewModel.getUserProfile(token)
    }

    LaunchedEffect(user) {
        Log.d("MainScreen", "Fetching user stats for user: ${user?.id} with token: $user")
        user?.let { viewModel.getUserStats(it.id, token) }
        showChart.value = false
        delay(300)
        Log.d("MainScreen", "Total tasks: $totalTasks, Completed tasks: $completedTasks, Most productive hour: $mostProductiveHour")
        showChart.value = true
    }
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = BackgroundColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(15.dp)
        ) {
            // Header con línea decorativa
            Text(
                text = "FocusPlanner",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .height(3.dp)
                    .background(AccentColor, shape = RoundedCornerShape(50))
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Grid 2x2 para las cards de navegación
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Fila 1: Crear tareas + Mis Tareas
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    NavCard(
                    icon = Icons.Default.List,
                    title = "Mis Tareas",
                    onClick = { onNavigate("tasks") },
                    modifier = Modifier.weight(1f)
                )
                    NavCard(
                        icon = Icons.Default.AddToPhotos,
                        title = "Crear tareas",
                        onClick = { onNavigate("addTask") },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Fila 2: Calendario + Pomodoro
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    NavCard(
                        icon = Icons.Default.CalendarToday,
                        title = "Calendario",
                        onClick = { onNavigate("calendar") },
                        modifier = Modifier.weight(1f)
                    )
                    NavCard(
                        icon = Icons.Default.Timer,
                        title = "Pomodoro",
                        onClick = { onNavigate("pomodoro") },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Fila 3: Perfil + Ajustes
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    NavCard(
                        icon = Icons.Default.Person,
                        title = "Perfil",
                        onClick = { onNavigate("profile") },
                        modifier = Modifier.weight(1f)
                    )
                    NavCard(
                        icon = Icons.Default.Settings,
                        title = "Ajustes",
                        onClick = { onNavigate("settings") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (showChart.value) {
                TaskPieChart(
                    totalTasks = totalTasks,
                    completedTasks = completedTasks
                )
            }

//            TaskBarChart()
//            TaskLineChart()
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

//Screen perfil
@Composable
fun TaskPieChart(
    totalTasks: Int,
    completedTasks: Int
) {
    val completedPercent = if (totalTasks > 0) {
        (completedTasks.toFloat() / totalTasks) * 100f
    } else 60f
    val pendingPercent = if (totalTasks > 0) {
        100f - completedPercent
    } else 40f

    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .padding(top = 24.dp),
        factory = { context ->
            PieChart(context).apply {
                description.isEnabled = false
                isRotationEnabled = true
                setUsePercentValues(true)
                setEntryLabelColor(android.graphics.Color.DKGRAY)
                setCenterText("Tareas")
                setCenterTextSize(18f)
                setCenterTextColor(android.graphics.Color.parseColor("#1565C0"))
                setHoleColor(android.graphics.Color.TRANSPARENT)
                animateY(1000)

                val entries = listOf(
                    PieEntry(completedPercent, "Completadas"),
                    PieEntry(pendingPercent, "Pendientes")
                )

                val dataSet = PieDataSet(entries, "")
                dataSet.colors = listOf(
                    android.graphics.Color.parseColor("#1565C0"), // azul medio
                    android.graphics.Color.parseColor("#64B5F6")  // azul claro
                )

                dataSet.valueTextColor = android.graphics.Color.BLACK
                dataSet.valueTextSize = 14f
                dataSet.sliceSpace = 3f
                dataSet.selectionShift = 5f

                val data = PieData(dataSet)
                data.setValueFormatter(PercentFormatter(this))
                this.data = data

                this.setDrawEntryLabels(true)
                this.setEntryLabelTextSize(12f)
                this.setEntryLabelColor(android.graphics.Color.BLACK)

                this.legend.isEnabled = true
                this.legend.textColor = android.graphics.Color.BLACK
                this.legend.textSize = 14f

                this.notifyDataSetChanged()
                this.invalidate()
            }
        }
    )
}


@Composable
fun TaskLineChart() {
    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .padding(top = 24.dp),
        factory = { context ->
            LineChart(context).apply {
                description.isEnabled = false
                animateX(1000)

                val entries = listOf(
                    Entry(0f, 1f),
                    Entry(1f, 3f),
                    Entry(2f, 2f),
                    Entry(3f, 5f),
                    Entry(4f, 4f),
                    Entry(5f, 2f),
                    Entry(6f, 6f)
                )

                val dataSet = LineDataSet(entries, "Tareas por día")
                dataSet.color = android.graphics.Color.parseColor("#1565C0")
                dataSet.setCircleColor(android.graphics.Color.parseColor("#1565C0"))
                dataSet.lineWidth = 2f
                dataSet.circleRadius = 4f
                dataSet.setDrawFilled(true)
                dataSet.fillColor = android.graphics.Color.parseColor("#1565C0")

                val data = LineData(dataSet)
                this.data = data

                val days = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")
                val xAxis = this.xAxis
                xAxis.valueFormatter = IndexAxisValueFormatter(days)
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.granularity = 1f
                xAxis.labelCount = days.size

                this.axisRight.isEnabled = false
                this.invalidate()
            }
        }
    )
}


@Composable
fun TaskBarChart() {
    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .padding(top = 24.dp),
        factory = { context ->
            BarChart(context).apply {
                description.isEnabled = false
                setFitBars(true)
                animateY(1000)

                val entries = listOf(
                    BarEntry(0f, 3f),
                    BarEntry(1f, 5f),
                    BarEntry(2f, 2f),
                    BarEntry(3f, 4f),
                    BarEntry(4f, 1f),
                    BarEntry(5f, 0f),
                    BarEntry(6f, 6f)
                )

                val dataSet = BarDataSet(entries, "Tareas completadas")
                dataSet.color = android.graphics.Color.parseColor("#1565C0")
                val barData = BarData(dataSet)
                barData.barWidth = 0.9f

                this.data = barData

                val days = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")
                val xAxis = this.xAxis
                xAxis.valueFormatter = IndexAxisValueFormatter(days)
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.setDrawGridLines(false)
                xAxis.granularity = 1f
                xAxis.labelCount = days.size

                this.axisLeft.axisMinimum = 0f
                this.axisRight.isEnabled = false
                this.invalidate()
            }
        }
    )
}

@Composable
fun NavCard(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(140.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = title, tint = AccentColor, modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                color = TextPrimary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}