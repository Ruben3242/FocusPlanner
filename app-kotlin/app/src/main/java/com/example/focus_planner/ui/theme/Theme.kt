package com.example.focus_planner.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Tu paleta de colores
val BackgroundColor = Color.White
val CardColor = Color.White
val TextPrimary = Color.Black
val TextSecondary = Color(0xFFB0B0B0)
val AccentColor = Color(0xFF1565C0)

private val LightColors = lightColorScheme(
    primary = AccentColor,
    onPrimary = Color.White,
    background = BackgroundColor,
    onBackground = TextPrimary,
    surface = CardColor,
    onSurface = TextPrimary,
    secondary = AccentColor,
    onSecondary = Color.White,
)
@Composable
fun FocusPlannerTheme(
    content: @Composable () -> Unit
) {

    MaterialTheme(
        colorScheme = LightColors,
        typography = Typography(
            titleMedium = TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            ),

            titleLarge = TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            ),
        ),
        shapes = Shapes(
            small = RoundedCornerShape(12.dp),
            medium = RoundedCornerShape(20.dp),
            large = RoundedCornerShape(0.dp)
        ),
        content = content
    )
}