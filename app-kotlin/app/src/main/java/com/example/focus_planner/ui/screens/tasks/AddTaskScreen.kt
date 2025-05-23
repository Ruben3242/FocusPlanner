package com.example.focus_planner.ui.screens.tasks


import android.app.DatePickerDialog
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import com.example.focus_planner.data.model.task.Task
import com.example.focus_planner.data.model.task.TaskPriority
import com.example.focus_planner.data.model.task.TaskStatus
import com.example.focus_planner.ui.components.TopBarWithClose
import com.example.focus_planner.utils.SharedPreferencesManager
import com.example.focus_planner.viewmodel.TaskViewModel
import java.util.Calendar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import androidx.compose.foundation.Image

@Composable
fun AddTaskScreen(
    onTaskAdded: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TaskViewModel = hiltViewModel()
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("") }
    var status by remember { mutableStateOf(TaskStatus.PENDING) } // Puedes agregar las opciones del enum de status
    var priority by remember { mutableStateOf(TaskPriority.MEDIUM) } // Puedes agregar las opciones del enum de prioridad
    var isCompleted by remember { mutableStateOf(false) }

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var videoUri by remember { mutableStateOf<Uri?>(null) }
    var audioUri by remember { mutableStateOf<Uri?>(null) }

    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> imageUri = uri }

    val videoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> videoUri = uri }

    val audioLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> audioUri = uri }

    val context = LocalContext.current
    val creationSuccess by viewModel.creationSuccess.collectAsState()
    val userId = SharedPreferencesManager.getUserId(context)



    // Navegación o feedback tras creación exitosa
    LaunchedEffect(creationSuccess) {
        when (creationSuccess) {
            true -> {
                viewModel.resetState()
                onTaskAdded()
            }
            false -> {
                // Aquí podrías mostrar un mensaje de error
                viewModel.resetState()
                Toast.makeText(context, "Error al crear tarea", Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }
    Scaffold(
        topBar = {
            TopBarWithClose(
                title = "Crear Tarea",
                onCloseClick = onBackClick
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

//        val textFieldColors = TextFieldDefaults.outlinedTextFieldColors(
//            cursorColor = MaterialTheme.colorScheme.primary,
//            focusedBorderColor = MaterialTheme.colorScheme.primary,
//            unfocusedBorderColor = Color.Gray
//        )
            val textFieldColors = TextFieldDefaults.colors(
                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                unfocusedIndicatorColor = Color.Gray,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                cursorColor = MaterialTheme.colorScheme.primary
            )
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Título") },
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors
            )


            DatePickerField(
                selectedDate = dueDate,
                onDateSelected = { dueDate = it }
            )

//            // Dropdowns para Estado y Prioridad
//            DropdownSelector(
//                label = "Estado",
//                options = TaskStatus.entries.map { it.name },
//                selectedValue = status.name,
//                onValueChanged = { status = it?.let { it1 -> TaskStatus.valueOf(it1) }!! },
//                modifier = Modifier.fillMaxWidth()
//            )

            DropdownSelector(
                label = "Prioridad",
                options = TaskPriority.entries.map { it.name },
                selectedValue = priority.name,
                onValueChanged = { priority = it?.let { it1 -> TaskPriority.valueOf(it1) }!! },
                modifier = Modifier.fillMaxWidth()
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = isCompleted,
                    onCheckedChange = { isCompleted = it }
                )
                Text("Completada")
            }
//            Spacer(modifier = Modifier.height(16.dp))
//
//            Text("Adjuntar archivos:", fontWeight = FontWeight.Bold)
//            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
//                IconButton(onClick = { imageLauncher.launch("image/*") }) {
//                    Icon(Icons.Default.Image, contentDescription = "Imagen")
//                }
//                IconButton(onClick = { videoLauncher.launch("video/*") }) {
//                    Icon(Icons.Default.VideoLibrary, contentDescription = "Vídeo")
//                }
//                IconButton(onClick = { audioLauncher.launch("audio/*") }) {
//                    Icon(Icons.Default.Mic, contentDescription = "Audio")
//                }
//            }
//
//            // Previsualizaciones
//            imageUri?.let {
//                Text("Imagen seleccionada:")
//                Image(
//                    painter = rememberAsyncImagePainter(it),
//                    contentDescription = null,
//                    modifier = Modifier
//                        .size(120.dp)
//                        .padding(top = 8.dp)
//                )
//            }
//
//            videoUri?.let {
//                Text("Vídeo seleccionado: ${it.lastPathSegment}", fontSize = 14.sp)
//            }
//
//            audioUri?.let {
//                Text("Audio seleccionado: ${it.lastPathSegment}", fontSize = 14.sp)
//            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (title.isBlank() || description.isBlank()) {
                        Toast.makeText(
                            context,
                            "Título y descripción son obligatorios",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        val newTask = userId?.let {
                            Task(
                                title = title,
                                description = description,
                                completed = isCompleted,
                                dueDate = dueDate,
                                status = status,
                                priority = priority,
                                googleCalendarEventId = "",
                                userId = it
                            )
                        }
                        if (newTask != null) {
                            viewModel.createTask(newTask, context)
                        } else {
                            Toast.makeText(
                                context,
                                "Error al obtener el ID de usuario",
                                Toast.LENGTH_SHORT
                            ).show()
                            Log.e("CreateTask", "Error al obtener el ID de usuario" + newTask)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Crear Tarea")
            }
        }
    }
}

@Composable
fun DatePickerField(
    label: String = "Fecha de vencimiento",
    selectedDate: String,
    onDateSelected: (String) -> Unit
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val selected = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                onDateSelected(selected)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(
                    width = 1.dp,
                    color = Color.Gray,
                    shape = RoundedCornerShape(2.dp)
                )
                .clickable { datePickerDialog.show() }
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Text(
                text = if (selectedDate.isNotEmpty()) selectedDate else "Seleccionar fecha",
                style = MaterialTheme.typography.bodyLarge,
                color = if (selectedDate.isNotEmpty()) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

