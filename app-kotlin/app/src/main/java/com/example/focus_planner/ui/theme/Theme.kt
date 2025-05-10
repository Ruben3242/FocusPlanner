package com.example.focus_planner.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Colores personalizados
val BluePrimary = Color(0xFF1565C0)
val BlueSecondary = Color(0xFF90CAF9)
val GrayBackground = Color(0xFFF5F5F5)
val DarkText = Color(0xFF212121)
val LightText = Color(0xFFFFFFFF)

private val LightColorScheme = lightColorScheme(
    primary = BluePrimary,
    onPrimary = LightText,
    secondary = BlueSecondary,
    background = GrayBackground,
    surface = Color.White,
    onBackground = DarkText,
    onSurface = DarkText,
)

private val DarkColorScheme = darkColorScheme(
    primary = BluePrimary,
    onPrimary = LightText,
    secondary = BlueSecondary,
    background = GrayBackground,
    surface = Color.White,
    onBackground = DarkText,
    onSurface = DarkText,
)

@Composable
fun FocusPlannerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content
    )
}