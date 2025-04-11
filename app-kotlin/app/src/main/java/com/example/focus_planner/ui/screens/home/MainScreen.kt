package com.example.focus_planner.ui.screens.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign

@Composable
fun MainScreen(onNavigate: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "FocusPlanner",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        // Tarjetas del menú
        MenuCard("📋 Mis Tareas", "tasks", onNavigate)
        MenuCard("📅 Calendario", "calendar", onNavigate)
        MenuCard("👤 Perfil", "profile", onNavigate)
    }
}

@Composable
fun MenuCard(title: String, route: String, onNavigate: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onNavigate(route) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF6200EE))
    ) {
        Text(
            text = title,
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMainScreen() {
    MainScreen {}
}
