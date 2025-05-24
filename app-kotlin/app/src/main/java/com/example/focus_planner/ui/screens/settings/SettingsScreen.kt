package com.example.focus_planner.ui.screens.settings

import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.focus_planner.data.model.User
import com.example.focus_planner.data.model.task.TaskStatus
import com.example.focus_planner.utils.SharedPreferencesManager.clearSession
import com.example.focus_planner.utils.SharedPreferencesManager.getToken
import com.example.focus_planner.utils.SharedPreferencesManager.getUserId
import com.example.focus_planner.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun SettingsTopBar(navController: NavHostController) {
    val barHeight = 48.dp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(barHeight)
            .background(MaterialTheme.colorScheme.primary)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween

    ) {
        Text(
            text = "Ajustes",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onPrimary
        )
        IconButton(onClick = { navController.navigate("home") }) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Volver",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
fun SettingsScreen(
    navController: NavHostController,
    viewModel: SettingsViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val exportedJson by viewModel.exportedJson.collectAsState()
    val importResult by viewModel.importResult.collectAsState()
    var isDialogVisible by remember { mutableStateOf(false) }
    var showStatusDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showImportConfirmationDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }



    val token = getToken(context)
    val user by viewModel.user.collectAsState()

    val isAutoDeleteEnabled = remember { mutableStateOf(false) }

    LaunchedEffect(user) {
        user?.let {
            isAutoDeleteEnabled.value = it.removeCompletedExpiredTasks
        }
    }

    LaunchedEffect(Unit) {
        val userId = getUserId(context)
        if (user == null && userId != null && token != null) {
            viewModel.loadUserById(userId, token)
        }
        viewModel.loadSelectedStatuses(context)
    }
    LaunchedEffect(importResult) {
        if (importResult?.isNotEmpty() == true) {
            Toast(context).apply {
                setText(importResult)
                show()
            }
            viewModel.clearImportResult()
        }
    }

    // Lanzador para abrir selector de archivos para importar JSON
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            // Leer contenido JSON desde uri y enviar a importTasks
            val json = context.contentResolver.openInputStream(uri)?.bufferedReader().use { it?.readText() }
            json?.let {
                viewModel.importTasks(it, context)
            }
        }
    }
    if (user == null) {
        // Usuario aún no cargado, mostrar loading
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    if (showDeleteDialog) {
        DeleteAccountDialog(
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                if (token != null) {
                    viewModel.deleteUserAccount(token, context)
                }
                showDeleteDialog = false
            }
        )
    }
    LaunchedEffect(viewModel.deleteSuccess.collectAsState().value) {
        if (viewModel.deleteSuccess.value) {
            navController.navigate("login") {
                popUpTo("home") { inclusive = true }
            }
            Toast.makeText(context, "Cuenta eliminada", Toast.LENGTH_SHORT).show()
        }
    }


    Scaffold(
        topBar = {
            SettingsTopBar(navController)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 11.dp)
                .verticalScroll(rememberScrollState())
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- Header con título y botón Cerrar sesión ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Preferencias generales", style = MaterialTheme.typography.titleMedium)

                TextButton(
                    onClick = {
                        clearSession(context)
                        navController.navigate("login") {
                            popUpTo("home") { inclusive = true }
                        }
                    },
                    modifier = Modifier.height(40.dp),
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Logout, contentDescription = "Cerrar sesión")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cerrar sesión", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
            }

            // --- Preferencias Generales ---
//            Text(text = "Preferencias generales", style = MaterialTheme.typography.titleMedium)
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoDelete,
                                contentDescription = "Eliminación automática",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(end = 12.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Eliminación automática de tareas", style = MaterialTheme.typography.titleSmall)
                                Text(
                                    text = "Elimina automáticamente tareas con estados seleccionados.",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Switch(
                                checked = isAutoDeleteEnabled.value,
                                onCheckedChange = { newValue ->
                                    isAutoDeleteEnabled.value = newValue
                                    user?.let { currentUser ->
                                        viewModel.updateUserSettings(currentUser.id, token!!, newValue)
                                    }
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { showStatusDialog = true },
                            modifier = Modifier.align(Alignment.End),
                            enabled = isAutoDeleteEnabled.value,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isAutoDeleteEnabled.value)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.surface,
                                contentColor = if (isAutoDeleteEnabled.value)
                                    MaterialTheme.colorScheme.onPrimary
                                else
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        ) {
                            Text("Seleccionar estados")
                        }
                    }
                }

                // Dialogo selección estados eliminación
                if (showStatusDialog) {
                    AlertDialog(
                        onDismissRequest = { showStatusDialog = false },
                        confirmButton = {
                            TextButton(onClick = {
                                if (viewModel.selectedStatuses.isNotEmpty() && token != null) {
                                    viewModel.deleteTasksByStatuses(token, viewModel.selectedStatuses.toList())
                                }
                                showStatusDialog = false
                            }) {
                                Text("Eliminar")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showStatusDialog = false }) {
                                Text("Cancelar")
                            }
                        },
                        title = { Text("Selecciona estados a eliminar") },
                        text = {
                            Column {
                                TaskStatus.values()
                                    .filter { it != TaskStatus.PENDING }
                                    .forEach { status ->
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Checkbox(
                                                checked = viewModel.selectedStatuses.contains(status),
                                                onCheckedChange = {
                                                    viewModel.toggleStatusSelection(context, status)
                                                }
                                            )
                                            Text(text = status.name)
                                        }
                                    }
                            }
                        }
                    )
                }
            }

            // --- Importación / Exportación y Sincronización ---
            Text(text = "Importación y sincronización", style = MaterialTheme.typography.titleMedium)
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SettingsOptionCard(
                    icon = Icons.Default.Download,
                    title = "Exportar tareas",
                    description = "Descarga tus tareas en un archivo JSON",
                    onClick = { viewModel.exportTasks(context) }
                )

                SettingsOptionCard(
                    icon = Icons.Default.Upload,
                    title = "Importar tareas",
                    description = "Sube un archivo JSON con tus tareas",
                    onClick = { showImportConfirmationDialog = true }
                )

                if (showImportConfirmationDialog) {
                    AlertDialog(
                        onDismissRequest = { showImportConfirmationDialog = false },
                        title = { Text("¿Estás seguro?") },
                        text = {
                            Text(
                                "Estás a punto de importar todas las tareas del archivo seleccionado.\n\n" +
                                        "Si tu cuenta está vinculada con Google Calendar, las tareas también se añadirán automáticamente a tu calendario.\n\n" +
                                        "Podrás gestionarlas desde la sección 'Mis tareas'."
                            )
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    showImportConfirmationDialog = false
                                    importLauncher.launch("application/json")
                                }
                            ) { Text("Confirmar") }
                        },
                        dismissButton = {
                            TextButton(onClick = { showImportConfirmationDialog = false }) {
                                Text("Cancelar")
                            }
                        }
                    )
                }

                SettingsOptionCard(
                    icon = Icons.Default.CalendarToday,
                    title = "Integración con Google Calendar",
                    description = "Activa la sincronización de tareas con tu calendario de Google.",
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse("https://9679-92-189-98-92.ngrok-free.app/oauth2/authorization/google")
                        }
                        context.startActivity(intent)
                    }
                )
            }

            // --- Información y ayuda ---
            Text(text = "Información y ayuda", style = MaterialTheme.typography.titleMedium)
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SettingsOptionCard(
                    icon = Icons.Default.Help,
                    title = "Servicio de asistencia",
                    description = "¿Tienes dudas? Escríbenos por correo",
                    onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:focusplanner.welcome@gmail.com")
                            putExtra(Intent.EXTRA_SUBJECT, "Consulta desde la app FocusPlanner")
                        }
                        context.startActivity(intent)
                    }
                )

                SettingsOptionCard(
                    icon = Icons.Default.Info,
                    title = "Términos y condiciones",
                    description = "Consulta los términos de uso de Focus Planner",
                    onClick = { showTermsDialog = true }
                )

                if (showTermsDialog) {
                    AlertDialog(
                        onDismissRequest = { showTermsDialog = false },
                        confirmButton = {
                            TextButton(onClick = { showTermsDialog = false }) {
                                Text("Cerrar")
                            }
                        },
                        title = { Text("Términos y condiciones de Focus Planner") },
                        text = {
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .height(300.dp)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Text(
                                    text = """
                                    Bienvenido a Focus Planner. Al usar esta aplicación, aceptas los siguientes términos y condiciones:

                                    1. Uso de la Aplicación
                                    Focus Planner está diseñada para ayudarte a organizar y gestionar tus tareas de manera eficiente. Te comprometes a utilizar la app de manera responsable y conforme a la ley.

                                    2. Privacidad y Datos
                                    Tus datos personales y tus tareas se almacenan en nuestra base de datos y en tu dispositivo. Nos comprometemos a proteger tu privacidad y no compartir tu información con terceros sin tu consentimiento.

                                    3. Sincronización con Google Calendar
                                    La integración con Google Calendar permite añadir tareas a tu calendario. Sin embargo, los cambios que realices directamente en Google Calendar no se sincronizarán de vuelta a Focus Planner.

                                    4. Responsabilidad
                                    Focus Planner se proporciona "tal cual". No garantizamos la ausencia de errores o interrupciones. No nos hacemos responsables de la pérdida de datos o cualquier daño derivado del uso de la app.

                                    5. Cambios en los Términos
                                    Podemos actualizar estos términos en cualquier momento. Te recomendamos revisarlos periódicamente.

                                    6. Contacto
                                    Para cualquier duda o sugerencia, contacta con nuestro equipo de soporte.

                                    Gracias por confiar en Focus Planner para mejorar tu productividad.
                                """.trimIndent(),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    )
                }
            }

            exportedJson?.let { jsonString ->
                SaveFileButton(jsonString, onSaved = { viewModel.clearExportedJson() })
            }

            importResult?.let { result ->
                Text(text = result)
                LaunchedEffect(result) { viewModel.clearImportResult() }
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // --- Avanzado ---
            Text(text = "Avanzado", style = MaterialTheme.typography.titleMedium)
            SettingsOptionCard(
                icon = Icons.Default.Delete,
                title = "Eliminar cuenta",
                description = "Esta acción es irreversible",
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
                onClick = { showDeleteDialog = true }
            )
        }
    }

}

@Composable
fun SaveFileButton(jsonString: String, onSaved: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Button(onClick = {
        scope.launch {
            val fileName = "tasks_export_${System.currentTimeMillis()}.json"
            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)
            file.writeText(jsonString)
            // Puedes mostrar un Toast o Snackbar que el archivo se guardó
            onSaved()
        }
    }) {
        Text("Guardar archivo de exportación")
    }
}

@Composable
fun SettingsOptionCard(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier
//                    .size(40.dp)
                    .padding(end = 16.dp),
                tint = contentColor
            )
            Column {
                Text(text = title, style = MaterialTheme.typography.titleSmall)
                Text(text = description, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun DeleteAccountDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    var confirmationText by remember { mutableStateOf("") }
    val isConfirmEnabled = confirmationText == "CONFIRMAR"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "¿Eliminar cuenta?",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.error
            )
        },
        text = {
            Column {
                Text(
                    "Eliminar tu cuenta borrará toda tu información personal y todas las tareas asociadas. Esta acción no se puede deshacer.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Si deseas conservar tus tareas, puedes exportarlas desde Inicio > Ajustes > Exportar mis tareas.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Escribe CONFIRMAR para continuar:",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(4.dp))
                TextField(
                    value = confirmationText,
                    onValueChange = { confirmationText = it },
                    placeholder = { Text("CONFIRMAR") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = isConfirmEnabled,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Quiero eliminar mi cuenta")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

