package com.example.focus_planner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.focus_planner.ui.screens.LoginScreen
import com.example.focus_planner.ui.theme.Focus_plannerTheme
import com.example.focus_planner.viewmodel.UserViewModel
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.padding

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            Focus_plannerTheme {
                // Crear el NavHostController para manejar la navegación
                val navController = rememberNavController()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Configuración de la navegación
                    NavHost(
                        navController = navController,
                        startDestination = "login", // Pantalla inicial (Login)
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("login") {
                            // Llamada a la pantalla de Login
                            LoginScreen(
                                navController = navController,
                                viewModel = UserViewModel() // Aquí debes usar tu ViewModel
                            )
                        }
                        composable("task_list") {
                            // Aquí agregarás la pantalla del listado de tareas
                            // TaskListScreen() // Esta es una pantalla de ejemplo que podrías agregar
                        }
                        composable("register") {
                            // Aquí agregarías la pantalla de registro
                            // RegisterScreen() // Esta es una pantalla de ejemplo que podrías agregar
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    Focus_plannerTheme {
        // Vista previa de la pantalla de Login
        LoginScreen(navController = rememberNavController(), viewModel = UserViewModel())
    }
}
