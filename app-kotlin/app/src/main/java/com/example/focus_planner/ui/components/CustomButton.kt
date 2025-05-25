package com.example.focus_planner.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun <T> DropdownMenuBox(
    items: List<T>,
    selectedItem: T,
    itemToString: (T) -> String,
    onItemSelected: (T) -> Unit,
    isNumeric: Boolean = false // <- nuevo parámetro opcional
) {
    var expanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredItems = items.filter {
        itemToString(it).contains(searchQuery, ignoreCase = true)
    }

    Box {
        Text(
            text = itemToString(selectedItem),
            color = Color.White,
            modifier = Modifier
                .background(Color.Gray, RoundedCornerShape(6.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .clickable {
                    expanded = true
                    searchQuery = ""
                }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
                searchQuery = ""
            },
            modifier = Modifier.heightIn(max = 300.dp)
        ) {
            // Barra de búsqueda con teclado numérico si se indica
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar...") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = if (isNumeric) KeyboardType.Number else KeyboardType.Text
                ),
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
            )

            filteredItems.forEach { item ->
                DropdownMenuItem(
                    text = { Text(itemToString(item)) },
                    onClick = {
                        onItemSelected(item)
                        expanded = false
                        searchQuery = ""
                    }
                )
            }

            if (filteredItems.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("Sin resultados", color = Color.Gray) },
                    onClick = {},
                    enabled = false
                )
            }
        }
    }
}

