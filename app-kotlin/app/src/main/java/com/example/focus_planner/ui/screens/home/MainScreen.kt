package com.example.focus_planner.ui.screens.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavController
import com.example.focus_planner.utils.SharedPreferencesManager.clearSession
import com.example.focus_planner.utils.TokenManager

@Composable
fun MainScreen(onNavigate: (String) -> Unit) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        TokenManager.checkTokenAndRefresh(context, NavController(context))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "FocusPlanner",
            style = MaterialTheme.typography.headlineLarge.copy(color = Color(0xFF6200EE)),
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Tarjetas del menú
        MenuCard("📋 Mis Tareas", "tasks", onNavigate)
        MenuCard("📅 Calendario", "calendar", onNavigate)
        MenuCard("👤 Perfil", "profile", onNavigate)
        MenuCard("⏳ Pomodoro", "pomodoro", onNavigate)
        // Cerrar sesión
        MenuCard("🔒 Cerrar Sesión", "login",
            onNavigate = {
                clearSession(context)
                onNavigate("login") // Después de limpiar la sesión, navega a la pantalla de login
            }
        )
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
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp), // Agregada sombra para dar un efecto de profundidad
        colors = CardDefaults.cardColors(containerColor = Color(0xFF6200EE))
    ) {
        Text(
            text = title,
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            color = Color.White,
            textAlign = TextAlign.Center
        )
    }
}

// Preview de la pantalla principal
@Preview(showBackground = true)
@Composable
fun PreviewMainScreen() {
    MainScreen(onNavigate = {}) // Navegación ficticia para vista previa
}
