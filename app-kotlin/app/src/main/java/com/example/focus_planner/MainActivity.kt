package com.example.focus_planner

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable

import androidx.navigation.compose.rememberNavController
import com.example.focus_planner.data.api.RetrofitInstance
import com.example.focus_planner.data.auth.AuthManager
import com.example.focus_planner.data.token.TokenManager
import com.example.focus_planner.ui.theme.Focus_plannerTheme

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.focus_planner.navigation.AppNavigation
import com.jakewharton.threetenabp.AndroidThreeTen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            AppNavigation(navController)
        }
    }
}

@Composable
fun MainScreen() {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Text(text = "¡Jetpack Compose está activado!", style = MaterialTheme.typography.headlineMedium)
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMainScreen() {
    MainScreen()
}


//@Preview(showBackground = true)
//@Composable
//fun DefaultPreview() {
//    Focus_plannerTheme {
//        LoginScreen()
//    }
//}