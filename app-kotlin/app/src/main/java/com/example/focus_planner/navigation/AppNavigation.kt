package com.example.focus_planner.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.focus_planner.ui.auth.LoginScreen
import com.example.focus_planner.ui.auth.RegisterScreen
import com.example.focus_planner.ui.calendar.CalendarScreen
import com.example.focus_planner.ui.home.MainScreen
import com.example.focus_planner.ui.profile.ProfileScreen
import com.example.focus_planner.ui.tasks.TaskDetailScreen
import com.example.focus_planner.ui.tasks.TaskListScreen
import com.example.focus_planner.viewmodel.UserViewModel

@Composable
fun AppNavigation(navController: NavHostController) {
    val userViewModel: UserViewModel = viewModel()

    NavHost(navController = navController, startDestination = "login") {
        composable("login") { LoginScreen(navController, userViewModel) }
        composable("register") { RegisterScreen(navController, userViewModel) }
        composable("home") { MainScreen { route -> navController.navigate(route) } }
        composable("tasks") { TaskListScreen { taskId -> navController.navigate("taskDetail/$taskId") } }
        composable("taskDetail/{taskId}") { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId")
            TaskDetailScreen(taskId = taskId)
        }
        composable("calendar") { CalendarScreen() }
        composable("profile") { ProfileScreen() }
        // Aquí agregaremos más pantallas después.
    }
}