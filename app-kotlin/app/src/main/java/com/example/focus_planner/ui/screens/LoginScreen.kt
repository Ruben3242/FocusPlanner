package com.example.focus_planner.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.focus_planner.viewmodel.UserViewModel
import androidx.compose.ui.platform.LocalContext

import androidx.compose.runtime.LaunchedEffect // Añadido para efectos laterales

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: UserViewModel
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var loginFailed by remember { mutableStateOf(false) } // Estado para controlar el error

    // Contexto para Toast
    val context = LocalContext.current

    // Mostrar el Toast si el login falla
    LaunchedEffect(loginFailed) {
        if (loginFailed) {
            Toast.makeText(context, "Error al iniciar sesión", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Iniciar sesión", style = MaterialTheme.typography.bodyMedium)

        Spacer(modifier = Modifier.height(16.dp))

        // Campo de correo electrónico
        TextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            label = { Text("Correo electrónico") },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Campo de contraseña
        TextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            label = { Text("Contraseña") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation() // Esto ocultará la contraseña
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Botón de login
        Button(
            onClick = {
                if (email.isNotEmpty() && password.isNotEmpty()) {
                    // Llamar al método de login del ViewModel
                    isLoading = true
                    viewModel.login(email, password) { success ->
                        isLoading = false
                        if (success) {
                            navController.navigate("task_list") // Navegar al listado de tareas
                        } else {
                            loginFailed = true // Establecer que ha fallado el login
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
                Text("Iniciar sesión")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Enlace a la pantalla de registro
        TextButton(onClick = {
            navController.navigate("register") // Navegar a la pantalla de registro
        }) {
            Text("¿No tienes cuenta? Regístrate")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen(navController = rememberNavController(), viewModel = UserViewModel())
}
