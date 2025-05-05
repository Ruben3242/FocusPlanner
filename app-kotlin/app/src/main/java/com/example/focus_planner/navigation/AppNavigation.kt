package com.example.focus_planner.navigation

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.focus_planner.ui.auth.LoginScreen
import com.example.focus_planner.ui.auth.RegisterScreen
import com.example.focus_planner.ui.calendar.CalendarScreen
import com.example.focus_planner.ui.screens.home.MainScreen
import com.example.focus_planner.ui.screens.profile.ProfileScreen
import com.example.focus_planner.ui.screens.tasks.TaskDetailScreen
import com.example.focus_planner.ui.screens.tasks.TaskListScreen
import com.example.focus_planner.utils.SharedPreferencesManager
import com.example.focus_planner.viewmodel.ProfileViewModel
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
        composable("tasks") { TaskListScreen { taskId -> navController.navigate("taskDetail/$taskId") } }
        composable("taskDetail/{taskId}") { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId")
            TaskDetailScreen(taskId = taskId)
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
