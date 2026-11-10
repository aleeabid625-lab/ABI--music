package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PurpleLight,
    onPrimary = Color.White,
    primaryContainer = PurpleDark,
    onPrimaryContainer = Color.White,
    secondary = BlueAccent,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF1E3A8A),
    onSecondaryContainer = Color.White,
    tertiary = CyanAccent,
    onTertiary = Color.Black,
    background = AmoledBackground,
    onBackground = TextWhite,
    surface = AmoledSurface,
    onSurface = TextWhite,
    surfaceVariant = AmoledSurfaceElevated,
    onSurfaceVariant = TextGray,
    outline = Color(0x338B5CF6),
    outlineVariant = Color(0x223B82F6)
)

@Composable
fun AbiMusicTheme(
    content: @Composable () -> Unit
) {
    // ABI MUSIC explicitly maintains the premium AMOLED Black + Purple/Blue identity
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}

// Backward-compatible alias for template
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    AbiMusicTheme(content = content)
}
