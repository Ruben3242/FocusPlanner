package com.example.focus_planner.ui.auth

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.focus_planner.viewmodel.UserViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(
    navController: NavController,
    viewModel: UserViewModel
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }

    var showDialog by remember { mutableStateOf(false) }
    var isSuccess by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    val context = LocalContext.current

    LaunchedEffect(message) {
        if (message.isNotEmpty()) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Registro", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Nombre") },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Correo electrónico") },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Contraseña") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (username.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                    isLoading = true
                    viewModel.register(username, email, password) { resultMessage ->
                        isLoading = false
                        if (resultMessage.contains("éxito", ignoreCase = true) || resultMessage.contains("verifica", ignoreCase = true)) {
                            isSuccess = true
                            dialogMessage = resultMessage
                            showDialog = true

                            // Redirigir al login tras un pequeño retraso
                            coroutineScope.launch {
                                delay(3000) // Esperar 3 segundos
                                navController.navigate("login") {
                                    popUpTo("register") { inclusive = true } // Eliminar pantalla de registro del back stack
                                }
                            }
                        } else {
                            isSuccess = false
                            dialogMessage = resultMessage
                            showDialog = true
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Registrarse")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = { navController.navigate("login") }) {
            Text("¿Ya tienes cuenta? Inicia sesión")
        }
    }
    RegisterResultDialog(
        showDialog = showDialog,
        success = isSuccess,
        message = dialogMessage,
        onDismiss = { showDialog = false },
        onNavigateToLogin = {
            showDialog = false
            navController.navigate("login") {
                popUpTo("register") { inclusive = true }
            }
        }
    )

}
@Composable
fun RegisterResultDialog(
    showDialog: Boolean,
    success: Boolean,
    message: String,
    onDismiss: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { onDismiss() },
            title = { Text(if (success) "Registro exitoso" else "Error en el registro") },
            text = { Text(message) },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (success) {
                            onNavigateToLogin()
                        } else {
                            onDismiss()
                        }
                    }
                ) {
                    Text("Entendido")
                }
            }
        )
    }
}

//@Preview(showBackground = true)
//@Composable
//fun PreviewRegisterScreen() {
//    RegisterScreen(
//        navController = rememberNavController(),
//        viewModel = UserViewModel(context = LocalContext.current,
//            userRepository = UserRepository(RetrofitInstance.getRetrofitInstance().create(ApiService::class.java))
//        )
//    )
//}
