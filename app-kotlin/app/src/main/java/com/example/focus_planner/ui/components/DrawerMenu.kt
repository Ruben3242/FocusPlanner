package com.example.focus_planner.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun AppDrawer(
    onNavigationItemSelected: (String) -> Unit,
    closeDrawer: () -> Unit
) {
    ModalDrawerSheet(
        modifier = Modifier.fillMaxHeight()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Aquí puedes agregar las opciones del menú
            TextButton(onClick = { onNavigationItemSelected("tasks"); closeDrawer() }) {
                Text("Tareas")
            }
            TextButton(onClick = { onNavigationItemSelected("calendar"); closeDrawer() }) {
                Text("Calendario")
            }
            TextButton(onClick = { onNavigationItemSelected("profile"); closeDrawer() }) {
                Text("Perfil")
            }
        }
    }
}
