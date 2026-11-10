package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// AMOLED Black & Deep Midnight Backing
val AmoledBackground = Color(0xFF07060C)
val AmoledSurface = Color(0xFF100C1E)
val AmoledSurfaceElevated = Color(0xFF18122B)
val AmoledSurfaceHighlight = Color(0xFF241B3D)

// Neon & Vibrant Accents
val PurplePrimary = Color(0xFF9333EA)
val PurpleLight = Color(0xFFA855F7)
val PurpleDark = Color(0xFF6B21A8)
val PurpleAccent = Color(0xFF7C3AED)

val BlueAccent = Color(0xFF3B82F6)
val CyanAccent = Color(0xFF06B6D4)
val MagentaGlow = Color(0xFFD946EF)

// Text Colors
val TextWhite = Color(0xFFF8FAFC)
val TextGray = Color(0xFF94A3B8)
val TextMuted = Color(0xFF64748B)

// Status & Accents
val SuccessGreen = Color(0xFF10B981)
val FavoriteRed = Color(0xFFF43F5E)
val WarningAmber = Color(0xFFF59E0B)

// Gradients
val PurpleBlueGradient = Brush.horizontalGradient(
    colors = listOf(BlueAccent, PurpleAccent, PurpleLight)
)

val GlowGradient = Brush.linearGradient(
    colors = listOf(CyanAccent, PurpleLight, MagentaGlow)
)

val DarkSurfaceGradient = Brush.verticalGradient(
    colors = listOf(AmoledSurfaceElevated, AmoledSurface)
)

val CardBorderBrush = Brush.linearGradient(
    colors = listOf(
        Color(0x553B82F6),
        Color(0x668B5CF6),
        Color(0x2206B6D4)
    )
)
