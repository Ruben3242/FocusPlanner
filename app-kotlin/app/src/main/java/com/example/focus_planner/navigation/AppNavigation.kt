package com.example.focus_planner.navigation

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.focus_planner.data.repository.UserRepository
import com.example.focus_planner.network.ApiService
import com.example.focus_planner.network.RetrofitInstance
import com.example.focus_planner.ui.auth.LoginScreen
import com.example.focus_planner.ui.auth.RegisterScreen
import com.example.focus_planner.ui.calendar.CalendarScreen
import com.example.focus_planner.ui.home.MainScreen
import com.example.focus_planner.ui.profile.ProfileScreen
import com.example.focus_planner.ui.tasks.TaskDetailScreen
import com.example.focus_planner.ui.tasks.TaskListScreen
import com.example.focus_planner.utils.SharedPreferencesManager
import com.example.focus_planner.viewmodel.UserViewModel
import com.example.focus_planner.viewmodel.UserViewModelFactory

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
        composable("profile") { ProfileScreen() }
    }
}
