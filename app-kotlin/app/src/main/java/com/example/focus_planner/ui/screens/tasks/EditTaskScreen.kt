package com.example.focus_planner.ui.screens.tasks

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import com.example.focus_planner.data.model.task.TaskPriority
import com.example.focus_planner.data.model.task.TaskStatus
import com.example.focus_planner.viewmodel.TaskViewModel
import androidx.compose.ui.graphics.Color
import com.example.focus_planner.ui.components.TopBarWithClose


@Composable
fun EditTaskScreen(
    taskId: Long,
    onTaskUpdated: () -> Unit,
    onBackClick: () -> Unit,
    viewModel: TaskViewModel = hiltViewModel(),
    context: Context = LocalContext.current
) {
    LaunchedEffect(taskId) {
        viewModel.loadTaskDetail(taskId, context)
    }

    val task by viewModel.taskDetail.collectAsState()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("") }
    var status by remember { mutableStateOf(TaskStatus.PENDING) }
    var priority by remember { mutableStateOf(TaskPriority.MEDIUM) }
    var isCompleted by remember { mutableStateOf(false) }

    LaunchedEffect(task) {
        task?.let {
            title = it.title
            description = it.description ?: ""
            dueDate = it.dueDate ?: ""
            status = it.status
            priority = it.priority
            isCompleted = it.completed ?: false
        }
    }
    Scaffold(
        topBar = {
            TopBarWithClose(
                title = "Editar tarea",
                onCloseClick = onBackClick
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {


            styledTextField(title, { title = it }, "Título")
            styledTextField(description, { description = it }, "Descripción")

            DatePickerField(
                selectedDate = dueDate,
                onDateSelected = { dueDate = it }
            )
            val priorityOptions = TaskPriority.entries.map { it.displayName to it.value }

            DropdownSelector(
                label = "Prioridad",
                options = priorityOptions,
                selectedValue = priority.value,
                onValueChanged = { selected ->
                    priority = TaskPriority.entries.first { it.value == selected }
                },
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = isCompleted,
                    onCheckedChange = { isCompleted = it }
                )
                Text("¿Completada?")
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (title.isNotBlank() && description.isNotBlank()) {
                        val updatedTask = task?.copy(
                            title = title,
                            description = description,
                            dueDate = dueDate,
                            completed = isCompleted,
                            status = status,
                            priority = priority
                        )
                        updatedTask?.let {
                            viewModel.updateTask(updatedTask, context)
                            onTaskUpdated()
                        }
                    } else {
                        Toast.makeText(
                            context,
                            "Título y descripción requeridos",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Actualizar tarea")
            }
        }
    }
}

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