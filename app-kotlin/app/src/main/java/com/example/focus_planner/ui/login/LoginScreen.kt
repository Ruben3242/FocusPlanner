package com.example.focus_planner.ui.login

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.focus_planner.MainActivity
import com.example.focus_planner.data.api.AuthApi
import com.example.focus_planner.data.model.LoginRequest
import com.example.focus_planner.data.token.TokenManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Composable
fun LoginScreen(navController: NavController? = null) {
    val context = LocalContext.current
    TokenManager.init(context)

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val retrofit = Retrofit.Builder()
        .baseUrl("http://10.0.2.2:8080/")  // URL del backend
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val authApi = retrofit.create(AuthApi::class.java)

    fun login() {
        isLoading = true
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = authApi.login(LoginRequest(email, password))
                if (response.isSuccessful) {
                    val tokenResponse = response.body()
                    tokenResponse?.let {
                        TokenManager.saveTokens(it.token, it.refreshToken, System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000))
                        (context as? Activity)?.runOnUiThread {
                            context.startActivity(Intent(context, MainActivity::class.java))
                            (context as? Activity)?.finish()
                        }
                    }
                } else {
                    (context as? Activity)?.runOnUiThread {
                        Toast.makeText(context, "Error de login", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                (context as? Activity)?.runOnUiThread {
                    Toast.makeText(context, "Error de conexión", Toast.LENGTH_SHORT).show()
                }
            }
            isLoading = false
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Login", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Correo") })
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Contraseña") })

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { login() }, modifier = Modifier.fillMaxWidth(), enabled = !isLoading) {
            Text(if (isLoading) "Cargando..." else "Iniciar sesión")
        }
    }
}
