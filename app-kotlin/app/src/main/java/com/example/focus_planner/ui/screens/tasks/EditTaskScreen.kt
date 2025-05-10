package com.example.focus_planner.ui.screens.tasks

import android.app.DatePickerDialog
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import com.example.focus_planner.data.model.task.Task
import com.example.focus_planner.data.model.task.TaskPriority
import com.example.focus_planner.data.model.task.TaskStatus
import com.example.focus_planner.utils.SharedPreferencesManager
import com.example.focus_planner.viewmodel.TaskViewModel
import java.util.Calendar
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel


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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        // Encabezado
        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(
                onClick = onBackClick,
                modifier = Modifier.padding(end = 16.dp)
            ) {
                Text("Volver")
            }
            Text(
                text = "Editar tarea",
                style = MaterialTheme.typography.titleLarge,
                color = androidx.compose.ui.graphics.Color.Black
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Título") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Descripción") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        DatePickerField(
            selectedDate = dueDate,
            onDateSelected = { dueDate = it }
        )

        Spacer(modifier = Modifier.height(12.dp))

        DropdownSelector(
            label = "Estado",
            options = TaskStatus.entries.map { it.name },
            selectedValue = status.name,
            onValueChanged = { status = TaskStatus.valueOf(it!!) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        DropdownSelector(
            label = "Prioridad",
            options = TaskPriority.entries.map { it.name },
            selectedValue = priority.name,
            onValueChanged = { priority = TaskPriority.valueOf(it!!) }
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
                    Toast.makeText(context, "Título y descripción requeridos", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Actualizar tarea")
        }
    }
}
