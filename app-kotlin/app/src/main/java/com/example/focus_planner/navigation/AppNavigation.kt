package com.example.focus_planner.navigation

import android.content.Context
import android.util.Log
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.focus_planner.ui.auth.LoginScreen
import com.example.focus_planner.ui.auth.RegisterScreen
import com.example.focus_planner.ui.components.MainScaffold
import com.example.focus_planner.ui.screens.calendar.CalendarScreen
import com.example.focus_planner.ui.screens.home.MainScreen
import com.example.focus_planner.ui.screens.organizacion.PomodoroScreen
import com.example.focus_planner.ui.screens.profile.ProfileScreen
import com.example.focus_planner.ui.screens.settings.SettingsScreen
import com.example.focus_planner.ui.screens.tasks.AddTaskScreen
import com.example.focus_planner.ui.screens.tasks.EditTaskScreen
import com.example.focus_planner.ui.screens.tasks.TaskDetailScreen
import com.example.focus_planner.ui.screens.tasks.TaskListScreen
import com.example.focus_planner.utils.SharedPreferencesManager
import com.example.focus_planner.viewmodel.CalendarViewModel
import com.example.focus_planner.viewmodel.ProfileViewModel
import com.example.focus_planner.viewmodel.SettingsViewModel
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
    // Escuchamos la ruta actual para pasársela a MainScaffold
    val currentBackStackEntry = navController.currentBackStackEntryAsState()

    val currentRoute = currentBackStackEntry.value?.destination?.route ?: startDestination
    MainScaffold(navController = navController,currentRoute = currentRoute, content =  {

        NavHost(navController = navController, startDestination = startDestination) {
            composable("login") { LoginScreen(navController) }
            composable("register") { RegisterScreen(navController, userViewModel) }
            composable("home") {
                MainScreen(
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo("home") { inclusive = false }
                        }
                    },
                    navController = navController
                )

            }
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
                    onBackClick = { navController.navigate("home") },
                    modifier = Modifier,
                    token = token,
                    navController = navController
                )
            }

            composable(
                "taskDetailScreen/{taskId}",
                arguments = listOf(navArgument("taskId") { type = NavType.StringType })
            ) { backStackEntry ->
                val taskId = backStackEntry.arguments?.getString("taskId")
                TaskDetailScreen(navController = navController, taskId = taskId)
            }

            composable("calendar") {
                val calendarViewModel: CalendarViewModel = hiltViewModel()
                CalendarScreen(calendarViewModel,navController= navController)
            }
            // Aquí instanciamos ProfileViewModel usando viewModel() y pasando ApiService a través de RetrofitInstance
            composable("profile") {
                val profileViewModel: ProfileViewModel = hiltViewModel()
                ProfileScreen(
                    navController = navController,
                    viewModel = profileViewModel,
                    token = token ?: ""
                )
            }
            composable("addTask") {
                val taskViewModel: TaskViewModel = hiltViewModel()
                AddTaskScreen(
                    onTaskAdded = {
                        navController.navigate("tasks")
                    },
                    onBackClick = { navController.popBackStack() },
                    viewModel = taskViewModel,
                )
            }
            composable("editTask/{taskId}") { backStackEntry ->
                val taskId = backStackEntry.arguments?.getString("taskId")?.toLongOrNull()
                    ?: return@composable
                EditTaskScreen(
                    taskId = taskId,
                    onTaskUpdated = { navController.popBackStack() },
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable("pomodoro") {
                PomodoroScreen(
                    navController = navController,
                )
            }
            composable("settings") {
                val settingsViewModel: SettingsViewModel = hiltViewModel()
                SettingsScreen(navController, settingsViewModel) // Asegúrate de tener esta pantalla creada
            }

        }
    }
    )
}
