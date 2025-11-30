package com.sujan.jibeq.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = BluePrimary,
    secondary = BlueSecondary,
    background = BackgroundDark,
    surface = BackgroundDark,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    error = ErrorRed,
    onError = Color.White
)

@Composable
fun JibTrueTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme // we force dark; ignore light for now

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
