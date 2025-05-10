package com.example.focus_planner.ui.screens.profile

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.focus_planner.data.model.UpdateUserRequest
import com.example.focus_planner.data.model.User
import com.example.focus_planner.utils.SharedPreferencesManager
import com.example.focus_planner.utils.TokenManager
import com.example.focus_planner.viewmodel.ProfileViewModel

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

    var newUsername by remember { mutableStateOf("") }
    var newEmail by remember { mutableStateOf("") }
    var newFirstname by remember { mutableStateOf("") }
    var newLastname by remember { mutableStateOf("") }

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Botón de volver al inicio del contenido
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                Button(
                    onClick = { navController.popBackStack() }
                ) {
                    Text("Volver")
                }
            }

            if (user != null) {
                Text(
                    text = "Editar Perfil",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.Black
                )
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

            styledTextField(newUsername, { newUsername = it }, "Username")
            styledTextField(newEmail, { newEmail = it }, "Email")
            styledTextField(newFirstname, { newFirstname = it }, "Firstname")
            styledTextField(newLastname, { newLastname = it }, "Lastname")

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

            Text("¿Quieres activar la integración con Google Calendar?")
            Button(
                onClick = {
                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                        data =
                            android.net.Uri.parse("https://0522-92-189-98-92.ngrok-free.app/oauth2/authorization/google")
                    }
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5))
            ) {
                Text("Activar Google Calendar", color = Color.White)
            }
        }
    }
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
