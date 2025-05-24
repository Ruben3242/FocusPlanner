package com.example.focus_planner.ui.screens.profile

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.focus_planner.R
import com.example.focus_planner.data.model.UpdateUserRequest
import com.example.focus_planner.data.model.User
import com.example.focus_planner.ui.screens.home.TaskBarChart
import com.example.focus_planner.ui.screens.home.TaskLineChart
import com.example.focus_planner.ui.screens.home.TaskPieChart
import com.example.focus_planner.ui.screens.tasks.TaskListTopBar
import com.example.focus_planner.utils.SharedPreferencesManager
import com.example.focus_planner.utils.SharedPreferencesManager.loadProfileImageUri
import com.example.focus_planner.utils.SharedPreferencesManager.saveProfileImageUri
import com.example.focus_planner.utils.TokenManager
import com.example.focus_planner.viewmodel.ProfileViewModel
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import kotlinx.coroutines.delay

@Composable
fun ProfileTopBar(
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
            text = "Editar Perfil",
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
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel,
    token: String
) {
    val user by viewModel.userProfile.collectAsState(initial = null)
    val isLoading by viewModel.isLoading.collectAsState(initial = false)
    val errorMessage by viewModel.errorMessage.collectAsState(initial = null)
    val sharedPrefs =
        LocalContext.current.getSharedPreferences("focus_planner_prefs", Context.MODE_PRIVATE)
    val context = LocalContext.current

    var showPasswordPopup by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()


    var newUsername by remember { mutableStateOf("") }
    var newEmail by remember { mutableStateOf("") }
    var newFirstname by remember { mutableStateOf("") }
    var newLastname by remember { mutableStateOf("") }
    val showChart = remember { mutableStateOf(false) }

    val totalTasks by viewModel.totalTasks.collectAsState()
    val completedTasks by viewModel.completedTasks.collectAsState()
    val mostProductiveHour by viewModel.mostProductiveHour.collectAsState()

    LaunchedEffect(user) {
        user?.let {
            newUsername = it.username
            newEmail = it.email
            newFirstname = it.firstname.toString()
            newLastname = it.lastname.toString()
            viewModel.getUserStats(it.id, token)
        }
    }


    LaunchedEffect(Unit) {
        delay(1000) // espera 300 ms antes de mostrar el gráfico
        showChart.value = true
    }


    LaunchedEffect(Unit) {
        TokenManager.checkTokenAndRefresh(context, navController)
    }

    LaunchedEffect(token) {
        val token = sharedPrefs.getString("auth_token", null)
        if (!SharedPreferencesManager.isTokenExpired(context) && token != null) {
            viewModel.getUserProfile(token)
        }
    }

    LaunchedEffect(user) {
        user?.let {
            newUsername = it.username
            newEmail = it.email
            newFirstname = it.firstname.toString()
            newLastname = it.lastname.toString()
        }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        Scaffold(
            topBar = {
                ProfileTopBar(navController)
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(30.dp))
                ProfileImagePicker()

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

                Text(
                    text = "Perfil de ${user?.username ?: "Usuario"}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                styledTextField(newEmail, { newEmail = it }, "Gmail")
                styledTextField(newFirstname, { newFirstname = it }, "Nombre")
                styledTextField(newLastname, { newLastname = it }, "Apellidos")

                Button(
                    onClick = {
                        viewModel.updateUserProfile(
                            userId = user?.id ?: 0,
                            updatedUser = UpdateUserRequest(
                                username = newUsername,
                                email = newEmail,
                                firstname = newFirstname,
                                lastname = newLastname
                            ),
                            context = context,
                            navController = navController
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Guardar cambios")
                }
                Divider(modifier = Modifier.padding(vertical = 8.dp))

                UserStatsSection(
                    totalTasks = totalTasks,
                    completedTasks = completedTasks,
                    mostProductiveHour = mostProductiveHour
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (showChart.value) {
                    Text(
                        text = "Visualización gráfica de tu progreso y productividad",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray,
                        modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                    )

                    TaskPieChart(
                        totalTasks = totalTasks,
                        completedTasks = completedTasks
                    )

                    TaskBarChart()
                    TaskLineChart()
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp))

            }
        }
    }
}

@Composable
fun UserStatsSection(
    totalTasks: Int,
    completedTasks: Int,
    mostProductiveHour: Int?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Estadísticas de tu rendimiento en Focus Planner",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Aquí puedes ver un resumen de tus tareas y hábitos de productividad.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(8.dp))

        StatCard(
            title = "Tareas activas en este momento",
            value = "$totalTasks",
            icon = Icons.Default.List
        )

        StatCard(
            title = "Tareas completadas",
            value = "$completedTasks / $totalTasks",
            icon = Icons.Default.Check
        )

        mostProductiveHour?.let {
            StatCard(
                title = "Tu hora más productiva",
                value = "$it:00h",
                icon = Icons.Default.AccessTime,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}


@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = modifier
            .height(100.dp)
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = title, tint = Color(0xFF1E88E5))
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, fontWeight = FontWeight.Bold)
            }
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
fun TaskBarChart() {
    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .padding(top = 24.dp),
        factory = { context ->
            BarChart(context).apply {
                description.isEnabled = false
                axisRight.isEnabled = false

                val entries = listOf(
                    BarEntry(0f, 4f), // Lunes
                    BarEntry(1f, 2f), // Martes
                    BarEntry(2f, 6f), // Miércoles
                    BarEntry(3f, 3f), // Jueves
                    BarEntry(4f, 5f), // Viernes
                    BarEntry(5f, 1f), // Sábado
                    BarEntry(6f, 0f)  // Domingo
                )

                val dataSet = BarDataSet(entries, "Tareas por día")
                dataSet.color = android.graphics.Color.parseColor("#1E88E5")

                val barData = BarData(dataSet)
                barData.barWidth = 0.9f
                barData.setValueTextSize(12f)

                this.data = barData
                this.setFitBars(true)
                this.invalidate()
            }
        }
    )
}


@Composable
fun PasswordPopup(
    onDismiss: () -> Unit,
    onSubmit: (String, String, String, String) -> Unit,
    currentUserData: User, // Para acceder al email actual del usuario
    onError: (String) -> Unit // Para manejar errores en el popup
) {
    var email by remember { mutableStateOf("") }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var repeatPassword by remember { mutableStateOf("") }

    var currentDialog by remember { mutableStateOf(true) }  // Controlar cuál dialogo se muestra
    var errorText by remember { mutableStateOf<String?>(null) }

    // Estados para el control de visibilidad de las contraseñas
    var currentPasswordVisible by remember { mutableStateOf(false) }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var repeatPasswordVisible by remember { mutableStateOf(false) }

    // Función para alternar la visibilidad de las contraseñas
    fun togglePasswordVisibility(isVisible: Boolean, passwordType: String) {
        when (passwordType) {
            "current" -> currentPasswordVisible = !isVisible
            "new" -> newPasswordVisible = !isVisible
            "repeat" -> repeatPasswordVisible = !isVisible
        }
    }

    // Primer Dialog: Ingreso de correo y contraseña actual
    if (currentDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cambiar Contraseña", style = MaterialTheme.typography.titleLarge)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Cerrar")
                    }
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = currentPassword,
                        onValueChange = { currentPassword = it },
                        label = { Text("Contraseña actual") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        visualTransformation = if (currentPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = {
                                togglePasswordVisibility(
                                    currentPasswordVisible,
                                    "current"
                                )
                            }) {
                                Icon(
                                    imageVector = if (currentPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = "Mostrar/Ocultar Contraseña"
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    errorText?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = it, color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (email.isBlank() || currentPassword.isBlank()) {
                        errorText = "Todos los campos son obligatorios"
                    } else {
                        // Verifica si el correo ingresado es el del usuario actual
                        if (email != currentUserData.email) {
                            errorText = "El correo ingresado no coincide con el correo actual."
                            return@TextButton
                        }

                        // Si el correo es correcto, pasamos al segundo dialogo
                        errorText = null
                        currentDialog = false // Cerrar el primer dialog
                    }
                }) {
                    Text("Siguiente")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancelar")
                }
            }
        )
    } else {
        // Segundo Dialog: Cambio de contraseña
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Cambiar Contraseña", style = MaterialTheme.typography.titleLarge) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("Nueva contraseña") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = {
                                togglePasswordVisibility(
                                    newPasswordVisible,
                                    "new"
                                )
                            }) {
                                Icon(
                                    imageVector = if (newPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = "Mostrar/Ocultar Contraseña"
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = repeatPassword,
                        onValueChange = { repeatPassword = it },
                        label = { Text("Repetir nueva contraseña") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        visualTransformation = if (repeatPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = {
                                togglePasswordVisibility(
                                    repeatPasswordVisible,
                                    "repeat"
                                )
                            }) {
                                Icon(
                                    imageVector = if (repeatPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = "Mostrar/Ocultar Contraseña"
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    errorText?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = it, color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newPassword != repeatPassword) {
                        errorText = "Las contraseñas no coinciden"
                    } else {
                        // Llamar al método para actualizar la contraseña
                        onSubmit("", "", newPassword, repeatPassword)
                        onDismiss() // Cerrar el diálogo después de la actualización
                    }
                }) {
                    Text("Actualizar Contraseña")
                }
            },
            dismissButton = {}
        )
    }
}


@Composable
fun ChangePasswordDialog(
    onDismiss: () -> Unit,
    onPasswordChanged: (String) -> Unit
) {
    var showAuthDialog by remember { mutableStateOf(true) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var authError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    // Primer diálogo - Autenticación (correo y contraseña)
    if (showAuthDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Autenticación") },
            text = {
                Column {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Correo electrónico") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Contraseña actual") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    authError?.let {
                        Text(text = it, color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        authError = "Correo y contraseña son obligatorios"
                    } else {
                        // Aquí validas las credenciales (ej. llamar a un backend)
                        // Si la autenticación es correcta, muestra el siguiente diálogo
                        showAuthDialog = false
                        showChangePasswordDialog = true
                    }
                }) {
                    Text("Iniciar sesión")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Segundo diálogo - Cambio de contraseña
    if (showChangePasswordDialog) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Cambiar Contraseña") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("Nueva contraseña") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirmar nueva contraseña") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    passwordError?.let {
                        Text(text = it, color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newPassword.isBlank() || confirmPassword.isBlank()) {
                        passwordError = "Ambos campos son obligatorios"
                    } else if (newPassword != confirmPassword) {
                        passwordError = "Las contraseñas no coinciden"
                    } else {
                        // Aquí actualizas la contraseña (ej. llamar a un backend)
                        onPasswordChanged(newPassword)
                        onDismiss() // Cierra el diálogo después de la actualización
                    }
                }) {
                    Text("Actualizar Contraseña")
                }
            }
        )
    }
}
@Composable
fun ProfileImagePicker(
    modifier: Modifier = Modifier,
    defaultImageRes: Int = R.drawable.user,
) {
    val context = LocalContext.current
    var selectedImageUri by rememberSaveable {
        mutableStateOf(loadProfileImageUri(context)) // Cargamos desde prefs
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            // Persistimos el permiso
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: SecurityException) {
                e.printStackTrace()
            }

            // Guardamos en SharedPreferences
            selectedImageUri = uri
            saveProfileImageUri(context, uri)
        }
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Imagen circular
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(Color.LightGray)
                .clickable { launcher.launch(arrayOf("image/*")) } // abrimos documentos
        ) {
            if (selectedImageUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(model = selectedImageUri),
                    contentDescription = "Imagen de perfil",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                )
            } else {
                Image(
                    painter = painterResource(id = defaultImageRes),
                    contentDescription = "Imagen de perfil por defecto",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                )
            }
        }

        // Icono de edición estilizado
        Icon(
            imageVector = Icons.Default.Edit,
            contentDescription = "Cambiar imagen",
            tint = Color.White,
            modifier = Modifier
                .size(36.dp)
                .align(Alignment.BottomEnd)
                .offset(x = (-10).dp, y = (-10).dp)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFF4CAF50), Color(0xFF81C784))
                    ),
                    shape = CircleShape
                )
                .border(2.dp, Color.White, CircleShape)
                .padding(6.dp)
                .clickable { launcher.launch(arrayOf("image/*")) }
        )
    }
}



