package com.example.focus_planner.navigation

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.focus_planner.data.model.TaskPriority
import com.example.focus_planner.data.model.TaskStatus
import com.example.focus_planner.ui.auth.LoginScreen
import com.example.focus_planner.ui.auth.RegisterScreen
import com.example.focus_planner.ui.screens.calendar.CalendarScreen
import com.example.focus_planner.ui.screens.home.MainScreen
import com.example.focus_planner.ui.screens.profile.ProfileScreen
import com.example.focus_planner.ui.screens.tasks.TaskDetailScreen
import com.example.focus_planner.ui.screens.tasks.TaskListScreen
import com.example.focus_planner.utils.SharedPreferencesManager
import com.example.focus_planner.viewmodel.ProfileViewModel
import com.example.focus_planner.viewmodel.TaskViewModel
import com.example.focus_planner.viewmodel.UserViewModel

@Composable
fun AppNavigation(
    navController: NavHostController,
    userViewModel: UserViewModel
) {
    val context = LocalContext.current
    val token = SharedPreferencesManager.getToken(context)
    val tokenExpired = SharedPreferencesManager.isTokenExpired(context)

    val startDestination = if (token != null && !tokenExpired) "home" else "login"

    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") { LoginScreen(navController) }
        composable("register") { RegisterScreen(navController, userViewModel) }
        composable("home") { MainScreen { route -> navController.navigate(route) } }
        composable("tasks") {
            val context = LocalContext.current
            val taskViewModel: TaskViewModel = hiltViewModel()

            val token = SharedPreferencesManager.getToken(context) ?: ""

            // Importante: aquí seteas el token e inicializas los filtros
            LaunchedEffect(Unit) {
                taskViewModel.setToken(token)
                taskViewModel.initializeFiltering()
            }

            val tasks by taskViewModel.taskList.collectAsState()
            val loading by taskViewModel.loading.collectAsState()

            TaskListScreen(
                onTaskClick = { task ->
                    task.id?.let { taskId ->
                        navController.navigate("taskDetail/$taskId")
                    }
                },
                onBackClick = { navController.popBackStack() },
                modifier = Modifier,
                token = token,
                navController = navController
            )
        }

        composable("taskDetail/{taskId}") { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId")

            // Recupera el token desde SharedPreferences
            val context = LocalContext.current
            val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            val token = sharedPreferences.getString("token", null)

            if (token != null) {
                taskId?.let {
                    TaskDetailScreen(
                        navController = navController,
                        taskId = taskId,
                        token = "Bearer $token" // Asegúrate de anteponer "Bearer " si tu API lo necesita
                    )
                }
            } else {
                // Maneja el caso en que el token no esté disponible
                Log.e("TaskDetailScreen", "Token no disponible")
                // Puedes mostrar un mensaje de error o redirigir al usuario a la pantalla de inicio de sesión
                navController.navigate("login") {
                    popUpTo("login") { inclusive = true }
                }

            }
        }

        composable("calendar") { CalendarScreen() }
        // Aquí instanciamos ProfileViewModel usando viewModel() y pasando ApiService a través de RetrofitInstance
        composable("profile") {
            val profileViewModel: ProfileViewModel = hiltViewModel()
            ProfileScreen(
                navController = navController,
                viewModel = profileViewModel,
                token = token ?: ""
            )

        }
    }
}
