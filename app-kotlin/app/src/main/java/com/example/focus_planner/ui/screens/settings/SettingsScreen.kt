package com.example.focus_planner.ui.screens.settings

import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
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

    Scaffold(
        topBar = {
            SettingsTopBar(navController)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 24.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "Preferencias",
                style = MaterialTheme.typography.titleMedium
            )

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SettingsOptionCard(
                    icon = Icons.Default.CheckCircle,
                    title = "Eliminar tareas completadas",
                    description = "Borrar automáticamente las tareas una vez completadas",
                    onClick = {
                        // TODO: Activar switch en SettingsState
                    }
                )

                SettingsOptionCard(
                    icon = Icons.Default.Download,
                    title = "Exportar tareas",
                    description = "Descarga tus tareas en un archivo JSON",
                    onClick = {
                        viewModel.exportTasks(context)
                    }
                )

                SettingsOptionCard(
                    icon = Icons.Default.Upload,
                    title = "Importar tareas",
                    description = "Sube un archivo JSON con tus tareas",
                    onClick = {
                        importLauncher.launch("application/json")
                    }
                )

                SettingsOptionCard(
                    icon = Icons.Default.Help,
                    title = "Servicio de asistencia",
                    description = "¿Tienes dudas? Escríbenos por correo",
                    onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:soporte@focusplanner.com")
                            putExtra(Intent.EXTRA_SUBJECT, "Consulta desde la app FocusPlanner")
                        }
                        context.startActivity(intent)
                    }
                )
                exportedJson?.let { jsonString ->
                    SaveFileButton(jsonString, onSaved = {
                        viewModel.clearExportedJson()
                    })
                }

                // Mostrar mensaje resultado import
                importResult?.let { result ->
                    Text(text = result)
                    LaunchedEffect(result) {
                        // Limpid mensaje tras mostrar
                        viewModel.clearImportResult()
                    }
                }
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            Text(
                text = "Avanzado",
                style = MaterialTheme.typography.titleMedium
            )

            SettingsOptionCard(
                icon = Icons.Default.Delete,
                title = "Eliminar cuenta",
                description = "Esta acción es irreversible",
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
                onClick = {
                    // TODO: Mostrar diálogo de confirmación + deleteUserAccount()
                }
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
                    .size(28.dp)
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
