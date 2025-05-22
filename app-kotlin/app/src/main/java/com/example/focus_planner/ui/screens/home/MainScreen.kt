package com.example.focus_planner.ui.screens.home

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
import androidx.navigation.NavController
import com.example.focus_planner.utils.SharedPreferencesManager.clearSession
import com.example.focus_planner.utils.TokenManager

// Paleta colores
val BackgroundColor = Color.White // Gris oscuro más claro que antes
val CardColor = Color.White // Gris carbón suave para cartas
val TextPrimary = Color.Black
val TextSecondary = Color(0xFFB0B0B0)
val AccentColor = Color(0xFF1565C0) // Azul suave para acentos

@Composable
fun MainScreen(onNavigate: (String) -> Unit, navController: NavController) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        TokenManager.checkTokenAndRefresh(context, navController)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = BackgroundColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
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
                        icon = Icons.Default.CalendarToday,
                        title = "Calendario",
                        onClick = { onNavigate("calendar") },
                        modifier = Modifier.weight(1f)
                    )
                }
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
                        icon = Icons.Default.Timer,
                        title = "Pomodoro",
                        onClick = { onNavigate("pomodoro") },
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    NavCard(
                        icon = Icons.Default.Settings,
                        title = "Ajustes",
                        onClick = { onNavigate("settings") },
                        modifier = Modifier.weight(1f)
                    )
                    NavCard(
                        icon = Icons.Default.AddToPhotos,
                        title = "Crear tareas",
                        onClick = { onNavigate("addTask") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Botón Logout estilizado
            TextButton(
                onClick = {
                    clearSession(context)
                    onNavigate("login")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.textButtonColors(contentColor = AccentColor),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Logout, contentDescription = "Cerrar sesión")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cerrar sesión", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }
        }
    }
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