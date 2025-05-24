package com.example.focus_planner.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.focus_planner.ui.screens.home.AccentColor
import com.example.focus_planner.utils.SharedPreferencesManager.clearSession
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
    navController: NavHostController,
    content: @Composable () -> Unit,
    currentRoute: String
) {
    val context = LocalContext.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val navigateTo: (String) -> Unit = { route ->
        scope.launch { drawerState.close() }
        if (route == "login") {
            clearSession(context)
        }
        navController.navigate(route) {
            popUpTo("home") { inclusive = false }
            launchSingleTop = true
        }
    }

    // Condición para ocultar menú lateral en login y register
    val showDrawer = currentRoute != "login" && currentRoute != "register"

    if (showDrawer) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                // Limitamos el ancho del drawer para que no ocupe casi toda la pantalla
                ModalDrawerSheet(
                    modifier = Modifier.width(280.dp) // ancho ajustado, puedes probar otros valores
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                    ) {
                        Text(
                            "Menú",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.weight(1f)
                        )
                        // Botón para cerrar el drawer (bocadillo con flecha)
                        IconButton(onClick = { scope.launch { drawerState.close() } }) {
                            Icon(
                                imageVector = Icons.Default.ChevronLeft,
                                contentDescription = "Cerrar Menú",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Divider()
                    Spacer(modifier = Modifier.height(12.dp))
                    MenuCard("Inicio", "home",navigateTo)
                    MenuCard("Mis Tareas", "tasks", navigateTo)
                    MenuCard("Calendario", "calendar", navigateTo)
                    MenuCard("Perfil", "profile", navigateTo)
                    MenuCard("Pomodoro", "pomodoro", navigateTo)
                    MenuCard("Ajustes", "settings", navigateTo)

//                    Spacer(modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.height(250.dp))

                    // Botón Logout estilizado
                    TextButton(
                        onClick = {
                            clearSession(context)
                            navigateTo("login")
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
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("") },
                        navigationIcon = {
                            IconButton(
                                onClick = {
                                    scope.launch {
                                        if (drawerState.isClosed) drawerState.open()
                                        else drawerState.close()
                                    }
                                }
                            ) {
                                if (drawerState.isClosed) {
                                    Icon(Icons.Default.Menu, contentDescription = "Abrir Menú")
                                } else {
                                    Icon(Icons.Default.ChevronLeft, contentDescription = "Cerrar Menú")
                                }
                            }
                        }
                    )
                }
            ) { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding)) {
                    content()
                }
            }
        }
    } else {
        content()
    }
}

@Composable
fun MenuCard(
    title: String,
    route: String,
    onNavigate: (String) -> Unit
) {
    val icon = when (route) {
        "home" -> Icons.Default.Home
        "tasks" -> Icons.Default.List
        "calendar" -> Icons.Default.CalendarToday
        "profile" -> Icons.Default.Person
        "pomodoro" -> Icons.Default.Timer
        "settings" -> Icons.Default.Settings
        else -> Icons.Default.Menu
    }

    Card(
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .fillMaxWidth()
            .clickable { onNavigate(route) },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF374151)),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.padding(end = 16.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White
            )
        }
    }
}
