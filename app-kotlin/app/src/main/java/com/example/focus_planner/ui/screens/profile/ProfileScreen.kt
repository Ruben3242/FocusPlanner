package com.example.focus_planner.ui.screens.profile

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.focus_planner.data.model.UpdateUserRequest
import com.example.focus_planner.utils.SharedPreferencesManager
import com.example.focus_planner.utils.TokenManager
import com.example.focus_planner.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel,
    token: String
) {
    val user by viewModel.userProfile.collectAsState(initial = null)  // Usamos el userProfile
    val isLoading by viewModel.isLoading.collectAsState(initial = false)
    val errorMessage by viewModel.errorMessage.collectAsState(initial = null)
    val sharedPrefs = LocalContext.current.getSharedPreferences("focus_planner_prefs", Context.MODE_PRIVATE)


    val context = LocalContext.current
    LaunchedEffect(Unit) {
        TokenManager.checkTokenAndRefresh(context, navController)
    }
    var newUsername by remember { mutableStateOf("") }
    var newEmail by remember { mutableStateOf("") }
    var newFirstname by remember { mutableStateOf("") }
    var newLastname by remember { mutableStateOf("") }

    // Cargar el perfil al inicio
    LaunchedEffect(token) {
        val token = sharedPrefs.getString("auth_token", null)
        if (!SharedPreferencesManager.isTokenExpired(context) && token != null) {
            viewModel.getUserProfile(token)
        } else {
            Log.e("LoginFlow", "Token inválido o expirado. Saltando carga de perfil.")
        }
    }
    LaunchedEffect(user) {
        user?.let {
            newUsername = it.username
            newEmail = it.email
            newFirstname = it.firstname.toString()
            newLastname = it.lastname.toString()
        }
    }




    if (isLoading) {
        CircularProgressIndicator(modifier = Modifier.fillMaxSize())
    } else {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (user != null) {


                // Campos de texto para editar los datos
                OutlinedTextField(
                    value = newUsername,
                    onValueChange = { newUsername = it },
                    label = { Text("Username") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = newEmail,
                    onValueChange = { newEmail = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = newFirstname,
                    onValueChange = { newFirstname = it },
                    label = { Text("Firstname") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = newLastname,
                    onValueChange = { newLastname = it },
                    label = { Text("Lastname") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(onClick = {
                    viewModel.updateUserProfile(
                        userId = user?.id ?: 0,
                        updatedUser = UpdateUserRequest( // esto lo construyes con los datos del formulario
                            username = newUsername,
                            email = newEmail,
                            firstname = newFirstname,
                            lastname = newLastname
                        ),
                        context = context,
                        navController = navController
                    )
                }) {
                    Text("Guardar cambios")
                }
            } else {
                // Mostrar el mensaje de error
                errorMessage?.let {
                    Text(text = it, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun PasswordPopup(
    onDismiss: () -> Unit,
    onSubmit: (String, String, String, String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var repeatPassword by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Cambiar Contraseña")
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = "Cerrar")
                }
            }
        },
        text = {
            Column {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = currentPassword,
                    onValueChange = { currentPassword = it },
                    label = { Text("Contraseña Actual") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("Nueva Contraseña") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = repeatPassword,
                    onValueChange = { repeatPassword = it },
                    label = { Text("Repetir Nueva Contraseña") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSubmit(email, currentPassword, newPassword, repeatPassword)
            }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
@Preview(showBackground = true)
@Composable
fun PreviewProfileScreen() {
//    ProfileScreen(
//        rememberNavController()
//    )
}
