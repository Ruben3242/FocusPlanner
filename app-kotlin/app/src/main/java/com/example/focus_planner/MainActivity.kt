package com.example.focus_planner

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable

import androidx.navigation.compose.rememberNavController


import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import com.example.focus_planner.data.repository.UserRepository
import com.example.focus_planner.navigation.AppNavigation
import com.example.focus_planner.network.ApiService
import com.example.focus_planner.network.RetrofitInstance
import com.example.focus_planner.ui.theme.FocusPlannerTheme
import com.example.focus_planner.viewmodel.UserViewModel
import com.example.focus_planner.viewmodel.UserViewModelFactory
import com.jakewharton.threetenabp.AndroidThreeTen
import dagger.hilt.android.AndroidEntryPoint
import android.Manifest
import android.util.Log


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidThreeTen.init(this)
        // 🔔 Crear el canal de notificaciones
        createNotificationChannel()

        // Pedir permisos de notificación en Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                1
            )
        }

        // Inicializamos el UserRepository y el ViewModel
        val apiService = RetrofitInstance.getRetrofitInstance().create(ApiService::class.java)
        val userViewModel = ViewModelProvider(
            this,
            UserViewModelFactory(applicationContext)
        )[UserViewModel::class.java]


        setContent {
            val navController = rememberNavController()
            FocusPlannerTheme{
                AppNavigation(navController = navController, userViewModel = userViewModel)
            }
            if (intent?.getBooleanExtra("pomodoro", false) == true) {
                // Aquí puedes usar una variable compartida, una ViewModel o una función de navegación para cambiar de pantalla
                Log.d("MainActivity", "Abrir Pomodoro desde notificación")
            }

        }
    }
    // 🔧 Crear canal de notificación (Android 8+)
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Pomodoro Channel"
            val descriptionText = "Notificaciones de Pomodoro"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("pomodoro_channel", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}