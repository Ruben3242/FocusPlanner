package com.example.focus_planner.ui.screens.tasks

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.focus_planner.data.model.task.Task
import com.example.focus_planner.data.model.task.TaskPriority
import com.example.focus_planner.data.model.task.TaskStatus
import com.example.focus_planner.viewmodel.TaskViewModel
import androidx.compose.material3.TextFieldDefaults
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

@OptIn(ExperimentalMaterial3Api::class)
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
    var showCompleted by remember { mutableStateOf(false) }
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

            // Filtros desplegables
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DropdownSelector(
                    label = "Estado",
                    options = listOf(null) + TaskStatus.values().map { it.value },
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
                    options = listOf(null) + TaskPriority.values().map { it.value },
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
    var isExpanded by remember { mutableStateOf(false) }

    val maxSwipe = 300f
    val iconSize = 50.dp

    val draggableState = rememberDraggableState { delta ->
        offsetX = (offsetX + delta).coerceIn(-maxSwipe, maxSwipe)
    }

    val backgroundColor = when (task.priority) {
        TaskPriority.HIGH -> Color(0xFFFFCDD2) // rojo claro
        TaskPriority.MEDIUM -> Color(0xFFFFF9C4) // amarillo claro
        TaskPriority.LOW -> Color(0xFFC8E6C9) // verde claro
        else -> Color(0xFFE0E0E0) // gris claro por defecto
    }

    val statusIconColor = if (task.completed == true) Color(0xFF4CAF50) else Color.Gray

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .padding(vertical = 6.dp, horizontal = 12.dp)
    ) {
        // Fondo de acción: ELIMINAR
        if (offsetX < -30f) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Red.copy(alpha = 0.2f))
                    .padding(end = 16.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                IconButton(onClick = { onDeleteById(task.id) }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar tarea",
                        tint = Color.Red,
                        modifier = Modifier.size(iconSize)
                    )
                }
            }
        }
        // Fondo de acción: DETALLES
        else if (offsetX > 30f) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color(0xFF81C784).copy(alpha = 0.2f))
                    .padding(start = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                IconButton(onClick = { navController.navigate("editTask/${task.id}") }) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Ver/Editar tarea",
                        tint = Color(0xFF388E3C),
                        modifier = Modifier.size(iconSize)
                    )
                }
            }
        }

        // Tarjeta deslizante
        Card(
            modifier = Modifier
                .align(Alignment.Center)
                .offset { IntOffset(offsetX.toInt(), 0) }
                .draggable(
                    state = draggableState,
                    orientation = Orientation.Horizontal,
                    onDragStopped = {
                        offsetX = when {
                            offsetX > maxSwipe * 0.6f -> maxSwipe * 0.9f
                            offsetX < -maxSwipe * 0.6f -> -maxSwipe * 0.9f
                            else -> 0f
                        }
                    }
                )
                .clickable { isExpanded = !isExpanded },
            elevation = CardDefaults.cardElevation(4.dp),
            colors = CardDefaults.cardColors(containerColor = backgroundColor)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Estado tarea",
                        tint = statusIconColor,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = task.title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Vence: ${task.dueDate ?: "Sin fecha"}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                AnimatedVisibility(visible = isExpanded) {
                    Column(
                        modifier = Modifier
                            .padding(top = 10.dp)
                            .fillMaxWidth()
                    ) {
                        Divider(modifier = Modifier.padding(vertical = 4.dp))
                        Text(text = task.description ?: "Sin descripción")
                        Text(text = "Estado: ${task.status}")
                        Text(text = "Prioridad: ${task.priority}")
                        Button(
                            onClick = { navController.navigate("taskDetailScreen/${task.id}") },
                            modifier = Modifier
                                .align(Alignment.End)
                                .padding(top = 8.dp)
                        ) {
                            Text("Más información")
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun DropdownSelector(
    label: String,
    options: List<String?>,
    selectedValue: String?,
    onValueChanged: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        OutlinedTextField(
            value = selectedValue ?: "Todos",
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
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option ?: "Todos", color = Color.Black) },
                    onClick = {
                        onValueChanged(option)
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
