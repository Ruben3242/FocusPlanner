package com.example.focus_planner.ui.screens.tasks

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController

@Composable
fun TaskDetailScreen(taskId: String?) {
    val task = sampleTasks.find { it.id.toString() == taskId } ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = task.title, style = MaterialTheme.typography.headlineLarge, color = Color.Black)
        Spacer(modifier = Modifier.height(10.dp))
        Text(text = task.description, style = MaterialTheme.typography.bodyLarge, color = Color.DarkGray)
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewTaskDetailScreen() {
    TaskDetailScreen("1")
}
