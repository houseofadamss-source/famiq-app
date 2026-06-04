package com.famiq.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary            = GreenMain,
    onPrimary          = Color.White,
    primaryContainer   = Color(0xFF1A2E24),
    onPrimaryContainer = GreenMain,
    secondary          = GreenAccent,
    background         = BackgroundDark,
    onBackground       = Color(0xFFDCDCDC),
    surface            = SurfaceDark,
    onSurface          = Color(0xFFDCDCDC),
    surfaceVariant     = Color(0xFF222E28),
    outline            = Color(0xFF2E3D36),
)

private val LightColors = lightColorScheme(
    primary            = GreenAccent,
    onPrimary          = Color.White,
    primaryContainer   = GreenSoft,
    onPrimaryContainer = GreenDark,
    secondary          = GreenMid,
    background         = BackgroundLight,
    onBackground       = TextDark,
    surface            = SurfaceLight,
    onSurface          = TextDark,
    surfaceVariant     = Color(0xFFF0F0F0),
    outline            = Color(0xFFDDDDDD),
)

val LocalDarkMode = compositionLocalOf { false }

@Composable
fun CatatUangTheme(
    themePreference: String = "auto",
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val isDark = when (themePreference) {
        "light" -> false
        "dark" -> true
        else -> isSystemDark
    }

    CompositionLocalProvider(LocalDarkMode provides isDark) {
        MaterialTheme(
            colorScheme = if (isDark) DarkColors else LightColors,
            typography  = Typography,
            content     = content
        )
    }
}
