package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Signature Executive Navy & Gold Theme (Matches VML Recover Shield & Scales Identity)
private val VmlDarkColorScheme = darkColorScheme(
    primary = VmlGold,
    onPrimary = Color(0xFF161000),
    primaryContainer = VmlGoldContainer,
    onPrimaryContainer = VmlGoldLight,
    secondary = VmlGoldLight,
    onSecondary = Color(0xFF161000),
    secondaryContainer = VmlNavySurfaceVariant,
    onSecondaryContainer = VmlGoldLight,
    tertiary = VmlGreen,
    onTertiary = Color.White,
    background = VmlNavyBackground,
    onBackground = VmlTextPrimary,
    surface = VmlNavySurface,
    onSurface = VmlTextPrimary,
    surfaceVariant = VmlNavySurfaceVariant,
    onSurfaceVariant = VmlTextSecondary,
    outline = VmlNavyBorder,
    outlineVariant = VmlGoldDim,
    error = VmlRed,
    onError = Color.White,
    errorContainer = VmlRedContainer,
    onErrorContainer = Color(0xFFFFB4AB)
)

private val VmlLightColorScheme = lightColorScheme(
    primary = VmlGoldDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFF3D0),
    onPrimaryContainer = Color(0xFF261A00),
    secondary = VmlGold,
    onSecondary = Color(0xFF161000),
    tertiary = VmlGreen,
    onTertiary = Color.White,
    background = Color(0xFFF8FAFC),
    onBackground = VmlNavyDark,
    surface = Color.White,
    onSurface = VmlNavyDark,
    surfaceVariant = Color(0xFFEEF2F6),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCBD5E1),
    outlineVariant = VmlGold,
    error = VmlRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to signature Executive Navy & Gold theme
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) VmlDarkColorScheme else VmlLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
