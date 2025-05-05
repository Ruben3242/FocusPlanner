package com.example.focus_planner.ui.components


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.focus_planner.viewmodel.UserViewModel
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.platform.LocalContext

@Composable
fun ChangePasswordDialog(
    userViewModel: UserViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
    onDismiss: () -> Unit,
    onPasswordChangeSuccess: () -> Unit
) {
    var step by remember { mutableStateOf(1) }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var newPassword by remember { mutableStateOf("") }
    var confirmNewPassword by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current

    Dialog(onDismissRequest = { onDismiss() }) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            shadowElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Cambiar contraseña", style = MaterialTheme.typography.bodySmall)
                    IconButton(onClick = { onDismiss() }) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                when (step) {
                    1 -> {
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Correo") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Contraseña actual") },
                            modifier = Modifier.fillMaxWidth(),
                            visualTransformation = PasswordVisualTransformation()
                        )

                        errorMessage?.let {
                            Text(it, color = MaterialTheme.colorScheme.error)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(onClick = {
                            userViewModel.login(email, password, context ) { success ->
                                if (success) {
                                    step = 2
                                    errorMessage = null
                                } else {
                                    errorMessage = "Credenciales incorrectas"
                                }
                            }
                        }) {
                            Text("Verificar")
                        }
                    }

                    2 -> {
                        OutlinedTextField(
                            value = newPassword,
                            onValueChange = { newPassword = it },
                            label = { Text("Nueva contraseña") },
                            modifier = Modifier.fillMaxWidth(),
                            visualTransformation = PasswordVisualTransformation()
                        )

                        OutlinedTextField(
                            value = confirmNewPassword,
                            onValueChange = { confirmNewPassword = it },
                            label = { Text("Confirmar nueva contraseña") },
                            modifier = Modifier.fillMaxWidth(),
                            visualTransformation = PasswordVisualTransformation()
                        )

                        errorMessage?.let {
                            Text(it, color = MaterialTheme.colorScheme.error)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(onClick = {
                            if (newPassword == confirmNewPassword && newPassword.length >= 6) {
                                userViewModel.changePassword(email, newPassword,context) { success ->
                                    if (success) {
                                        onPasswordChangeSuccess()
                                    } else {
                                        errorMessage = "Error al cambiar contraseña"
                                    }
                                }
                            } else {
                                errorMessage = "Las contraseñas no coinciden o son muy cortas"
                            }
                        }) {
                            Text("Guardar nueva contraseña")
                        }
                    }
                }
            }
        }
    }
}
