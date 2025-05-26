package com.example.focus_planner.ui.screens.tasks

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.focus_planner.data.model.task.Task
import com.example.focus_planner.data.model.task.TaskPriority
import com.example.focus_planner.data.model.task.TaskStatus
import com.example.focus_planner.viewmodel.TaskViewModel
import com.example.focus_planner.utils.TokenManager


@Composable
fun TaskListTopBar(
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
            text = "Mis Tareas",
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
fun TaskListScreen(
    onTaskClick: (Task) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    token: String,
    viewModel: TaskViewModel = hiltViewModel(),
    navController: NavController,
    loading: Boolean = false,
) {
    val context = LocalContext.current
    LaunchedEffect(true) {
        viewModel.setToken(token)
        TokenManager.checkTokenAndRefresh(context, navController)
    }

    val tasks by viewModel.tasks.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf<String?>(null) }
    var priorityFilter by remember { mutableStateOf<String?>(null) }
    var showCompleted by remember { mutableStateOf(true) }


    Scaffold(
        topBar = {
            TaskListTopBar(navController)
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(8.dp)
        ) {
            @Composable
            fun styledTextField(value: String, onValueChange: (String) -> Unit, label: String) {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    label = { Text(label) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF1E88E5),
                        unfocusedBorderColor = Color(0xFF90A4AE),
                        cursorColor = Color.Black,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedLabelColor = Color(0xFF1E88E5),
                        unfocusedLabelColor = Color.Gray
                    )
                )
            }

            styledTextField(searchQuery, {
                searchQuery = it
                viewModel.onSearchQueryChange(it)
            }, "Buscar por título")



            Spacer(modifier = Modifier.height(8.dp))
            val statusFilterOptions = listOf("Todas" to null) + TaskStatus.entries.map { it.displayName to it.value }
            val priorityFilterOptions = listOf("Todas" to null) + TaskPriority.entries.map { it.displayName to it.value }

            // Filtros desplegables
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DropdownSelector(
                    label = "Estado",
                    options = statusFilterOptions,
                    selectedValue = statusFilter,
                    onValueChanged = {
                        statusFilter = it
                        viewModel.onStatusFilterChange(it)
                    },
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                DropdownSelector(
                    label = "Prioridad",
                    options = priorityFilterOptions,
                    selectedValue = priorityFilter,
                    onValueChanged = {
                        priorityFilter = it
                        viewModel.onPriorityFilterChange(it)
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Mostrar tareas completadas
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp, horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = showCompleted,
                        onCheckedChange = {
                            showCompleted = it
                            viewModel.setShowCompleted(it)
                        }
                    )
                    Text("Mostrar tareas completadas")
                }

                IconButton(
                    onClick = { navController.navigate("addTask") },
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = MaterialTheme.shapes.small
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Añadir tarea",
                        tint = Color.White
                    )
                }
            }


            Spacer(modifier = Modifier.height(16.dp))

            // Lista de tareas
            LazyColumn {
                if (loading && tasks.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                } else {
                    items(tasks) { task ->
                        TaskCard(
                            task = task,
                            onDeleteById = { id -> viewModel.deleteTask(context, id) },
                            onNavigateToDetails = { id -> navController.navigate("taskDetail/$id") },
                            modifier = Modifier.padding(bottom = 8.dp),
                            navController = navController
                        )
                    }
                    item {
                        if (!loading) {
                            Button(
                                onClick = { viewModel.loadNextPage() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text("Cargar más")
                            }
                        } else {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .padding(16.dp)
                            )
                        }
                    }
                }
            }

        }
    }

}


@Composable
fun TaskCard(
    task: Task,
    onDeleteById: (Long) -> Unit,
    onNavigateToDetails: (Long) -> Unit,
    modifier: Modifier = Modifier,
    navController: NavController
) {
    var offsetX by remember { mutableStateOf(0f) }
    val maxSwipe = 250f

    val backgroundColor = when (task.priority) {
        TaskPriority.HIGH -> Color(0xFF1E3A8A)
        TaskPriority.MEDIUM -> Color(0xFF334155)
        TaskPriority.LOW -> Color(0xFF64748B)
        else -> Color(0xFF1E293B)
    }

    val textColor = Color(0xFFE0E0E0)
    val iconColor = if (task.completed == true) Color(0xFF38BDF8) else Color(0xFF94A3B8)

    val draggableState = rememberDraggableState { delta ->
        offsetX = (offsetX + delta).coerceIn(-maxSwipe, maxSwipe)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        // Fondo eliminar
        if (offsetX < -30f) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Red.copy(alpha = 0.15f)),
                contentAlignment = Alignment.CenterEnd
            ) {
                IconButton(onClick = { onDeleteById(task.id) }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = Color.Red,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }
        // Fondo editar
        else if (offsetX > 30f) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color(0xFF38BDF8).copy(alpha = 0.15f)),
                contentAlignment = Alignment.CenterStart
            ) {
                IconButton(onClick = { navController.navigate("editTask/${task.id}") }) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar",
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }

        Card(
            modifier = Modifier
                .offset { IntOffset(offsetX.toInt(), 0) }
                .draggable(
                    state = draggableState,
                    orientation = Orientation.Horizontal,
                    onDragStopped = {
                        offsetX = when {
                            offsetX > maxSwipe * 0.5f -> maxSwipe * 0.9f
                            offsetX < -maxSwipe * 0.5f -> -maxSwipe * 0.9f
                            else -> 0f
                        }
                    }
                ),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(4.dp),
            colors = CardDefaults.cardColors(containerColor = backgroundColor)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Estado",
                        tint = iconColor,
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(30.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = task.title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        Text(
                            text = "Vence: ${task.dueDate ?: "Sin fecha"}",
                            fontSize = 13.sp,
                            color = textColor.copy(alpha = 0.7f)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))

                    // Aquí colocamos el badge de prioridad
                    PriorityBadge(priority = task.priority)
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = task.description ?: "Sin descripción",
                    fontSize = 14.sp,
                    color = textColor,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Estado: ${task.status.displayName}",
                        color = textColor,
                        fontWeight = FontWeight.Medium
                    )
                    OutlinedButton(
                        onClick = { navController.navigate("taskDetailScreen/${task.id}") },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = textColor),
                        border = BorderStroke(1.dp, textColor.copy(alpha = 0.6f))
                    ) {
                        Text("Detalles")
                    }
                }
            }
        }
    }
}

data class PriorityInfo(
    val label: String,
    val color: Color,
    val icon: ImageVector,
    val iconTint: Color
)


@Composable
fun PriorityBadge(priority: TaskPriority) {
    val priorityInfo = when (priority) {
        TaskPriority.HIGH -> PriorityInfo("ALTA", Color(0xFFDC2626), Icons.Default.Warning, Color.White)
        TaskPriority.MEDIUM -> PriorityInfo("MEDIA", Color(0xFFF59E0B), Icons.Default.PriorityHigh, Color.Black)
        TaskPriority.LOW -> PriorityInfo("BAJA", Color(0xFF10B981), Icons.Default.CheckCircle, Color.Black)
        else -> PriorityInfo("DESCONOCIDA", Color.Gray, Icons.Default.Info, Color.White)
    }


    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(priorityInfo.color, shape = RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = priorityInfo.icon,
            contentDescription = "Icono prioridad",
            tint = priorityInfo.iconTint,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = priorityInfo.label,
            color = priorityInfo.iconTint,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
    }
}

@Composable
fun DropdownSelector(
    label: String,
    options: List<Pair<String, String?>>,
    selectedValue: String?,
    onValueChanged: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedDisplay = options.find { it.second == selectedValue }?.first ?: "Todas"


    Box(modifier = modifier) {
        OutlinedTextField(
            value = selectedDisplay,
            onValueChange = {},
            label = { Text(label, color = Color.Gray) },
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.Gray)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF1E88E5), // azul
                unfocusedBorderColor = Color(0xFF90A4AE), // gris azulado
                focusedLabelColor = Color(0xFF1E88E5),
                unfocusedLabelColor = Color.Gray,
                cursorColor = Color.Black,
                disabledTextColor = Color.Black,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black
            )
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color.White)
        ) {
            options.forEach { (display, value) ->
                DropdownMenuItem(
                    text = { Text(display, color = Color.Black) },
                    onClick = {
                        onValueChanged(value)
                        expanded = false
                    }
                )
            }
        }
    }
}


@Composable
fun DropdownMenu(label: String, selectedValue: String, onValueChanged: (String) -> Unit) {
    val options = listOf("HIGH", "MEDIUM", "LOW") // Ejemplo de opciones
    var expanded by remember { mutableStateOf(false) }

    Box {
        TextField(
            value = selectedValue,
            onValueChange = {},
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
            }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onValueChanged(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
