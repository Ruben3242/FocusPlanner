package com.example.focus_planner.ui.components

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CalendarMonth
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.focus_planner.ui.screens.home.AccentColor
import com.example.focus_planner.utils.SharedPreferencesManager.clearSession
import com.example.focus_planner.utils.SharedPreferencesManager.getUserEmail
import com.example.focus_planner.utils.SharedPreferencesManager.getUserName
import com.example.focus_planner.utils.SharedPreferencesManager.loadProfileImageUri
import com.example.focus_planner.viewmodel.ProfileImageViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
    navController: NavHostController,
    content: @Composable () -> Unit,
    currentRoute: String,
    profileImageViewModel: ProfileImageViewModel
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


    val showDrawer = currentRoute != "login" && currentRoute != "register"

    val profileImageUri by profileImageViewModel.profileImageUri.collectAsState()


    if (showDrawer) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                Box(
                    modifier = Modifier.width(260.dp) // Aquí
                ) {
                    key(profileImageUri) {
                        ModernDrawerContent(
                            currentRoute = currentRoute,
                            onNavigate = navigateTo,
                            onLogout = {
                                clearSession(context)
                                navigateTo("login")
                            },
                            profileImageUri = profileImageUri
                        )
                    }
                }
            },
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
    currentRoute: String,
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
    val isHome = route == "home"
    val cardHeight = if (isHome) 70.dp else 56.dp
    val cardShape = if (isHome) RoundedCornerShape(16.dp) else RoundedCornerShape(12.dp)

    val isSelected = route == currentRoute
    val backgroundColor = if (isSelected) Color(0xFF60A5FA) else Color(0xFF374151)
    val contentColor = if (isSelected) Color.Black else Color.White

    Card(
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .fillMaxWidth()
            .height(cardHeight)
            .clickable { onNavigate(route) },
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(if (isSelected) 8.dp else 4.dp),
        shape = cardShape,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.padding(end = 16.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = contentColor
            )
        }
    }
}

@Composable
fun ModernDrawerContent(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit,
    profileImageUri: Uri?,
) {

    val DrawerBackground = Color(0xFF1F2937)       // Gris oscuro elegante
    val HeaderTextColor = Color.White              // Texto principal blanco
    val SubTextColor = Color(0xFF9CA3AF)           // Gris claro para subtexto
    val DividerColor = Color(0xFF374151)           // Divisor gris más claro
    val SelectedItemBackground = Color(0xFF374151) // Fondo seleccionado: gris más claro
    val SelectedItemColor = Color(0xFF60A5FA)       // Azul elegante
    val UnselectedItemColor = Color(0xFFD1D5DB)     // Texto no seleccionado: gris claro
    val LogoutColor = Color(0xFFEF4444)             // Rojo suave

    val menuItems = listOf(
        Triple("Inicio", "home", Icons.Default.Home),
        Triple("Mis Tareas", "tasks", Icons.Default.List),
        Triple("Calendario", "calendar", Icons.Default.CalendarMonth),
        Triple("Pomodoro", "pomodoro", Icons.Default.Timer),
        Triple("Perfil", "profile", Icons.Default.Person),
        Triple("Ajustes", "settings", Icons.Default.Settings)
    )
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .background(DrawerBackground)
            .padding(vertical = 24.dp)
            .padding(top = 30.dp)
    ) {
        // Encabezado
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clickable { onNavigate("profile") }
        ) {
//            if (profileImageUri != null) {
//                Image(
//                    painter = rememberAsyncImagePainter(
//                        model = ImageRequest.Builder(context)
//                            .data(profileImageUri)
//                            .crossfade(true)
//                            .build()
//                    ),
//                    contentDescription = "Avatar",
//                    modifier = Modifier
//                        .size(48.dp)
//                        .clip(CircleShape)
//                        .border(2.dp, SelectedItemColor, CircleShape)
//                )
//            } else {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Avatar",
                    tint = SelectedItemColor,
                    modifier = Modifier.size(48.dp),
                )
//            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                getUserName(context)?.let { Text(it, color = HeaderTextColor, fontWeight = FontWeight.Bold) }
                getUserEmail(context)?.let { Text(it, color = SubTextColor, fontSize = 12.sp) }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Divider(color = DividerColor)

        // Menú
        menuItems.forEach { (title, route, icon) ->
            val isSelected = route == currentRoute
            val backgroundColor = if (isSelected) SelectedItemBackground else Color.Transparent
            val contentColor = if (isSelected) SelectedItemColor else UnselectedItemColor

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigate(route) }
                    .background(backgroundColor)
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = contentColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = title,
                    color = contentColor,
                    fontSize = 16.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Divider(color = DividerColor)

        // Logout
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onLogout() }
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Logout,
                contentDescription = "Cerrar sesión",
                tint = LogoutColor
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text("Cerrar sesión", color = LogoutColor, fontWeight = FontWeight.SemiBold)
        }
    }
}

