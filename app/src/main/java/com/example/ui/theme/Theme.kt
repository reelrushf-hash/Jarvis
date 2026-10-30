package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val JarvisColorScheme = darkColorScheme(
    primary = JarvisCyan,
    onPrimary = Color(0xFF021017),
    primaryContainer = JarvisSurfaceElevated,
    onPrimaryContainer = JarvisCyanLight,
    secondary = JarvisCyanDark,
    onSecondary = Color(0xFF021017),
    secondaryContainer = JarvisSurface,
    onSecondaryContainer = JarvisTextPrimary,
    tertiary = JarvisAmber,
    onTertiary = Color(0xFF221500),
    background = JarvisBackground,
    onBackground = JarvisTextPrimary,
    surface = JarvisSurface,
    onSurface = JarvisTextPrimary,
    surfaceVariant = JarvisSurfaceElevated,
    onSurfaceVariant = JarvisTextSecondary,
    outline = JarvisBorder,
    error = JarvisRedAlert,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = JarvisColorScheme,
        typography = Typography,
        content = content
    )
}
