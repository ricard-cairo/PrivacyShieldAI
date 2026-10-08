package com.example.privacyshieldai.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/*
 * Paleta basada en el azul del icono (#1B5E9E / degradado #2A7BC8 -> #0D3B66).
 * No se usan los colores dinámicos de Android 12+ (Material You), que toman los tonos
 * del fondo de pantalla y daban textos marrones o morados según el móvil.
 * Se definen todos los roles para no heredar los tonos morados por defecto de Material 3.
 */

private val LightColors = lightColorScheme(
    primary = Color(0xFF1B5E9E),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD3E4FF),
    onPrimaryContainer = Color(0xFF001C38),
    secondary = Color(0xFF3F5F80),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD6E4F7),
    onSecondaryContainer = Color(0xFF0F1D2E),
    tertiary = Color(0xFF0D3B66),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFCFE5FF),
    onTertiaryContainer = Color(0xFF001D33),
    background = Color(0xFFFAFCFF),
    onBackground = Color(0xFF1A1C20),
    surface = Color(0xFFFAFCFF),
    onSurface = Color(0xFF1A1C20),
    surfaceVariant = Color(0xFFDEE3EB),
    onSurfaceVariant = Color(0xFF42474E),
    surfaceTint = Color(0xFF1B5E9E),
    outline = Color(0xFF72777F),
    outlineVariant = Color(0xFFC2C7CF),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF3F6FB),
    surfaceContainer = Color(0xFFEDF1F7),
    surfaceContainerHigh = Color(0xFFE7EBF2),
    surfaceContainerHighest = Color(0xFFE1E6EC),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFA2C9FF),
    onPrimary = Color(0xFF00315C),
    primaryContainer = Color(0xFF0D4A80),
    onPrimaryContainer = Color(0xFFD3E4FF),
    secondary = Color(0xFFBACBE0),
    onSecondary = Color(0xFF243445),
    secondaryContainer = Color(0xFF3A4A5C),
    onSecondaryContainer = Color(0xFFD6E4F7),
    tertiary = Color(0xFF9CCBFF),
    onTertiary = Color(0xFF003354),
    tertiaryContainer = Color(0xFF0D3B66),
    onTertiaryContainer = Color(0xFFCFE5FF),
    background = Color(0xFF111418),
    onBackground = Color(0xFFE1E2E8),
    surface = Color(0xFF111418),
    onSurface = Color(0xFFE1E2E8),
    surfaceVariant = Color(0xFF42474E),
    onSurfaceVariant = Color(0xFFC2C7CF),
    surfaceTint = Color(0xFFA2C9FF),
    outline = Color(0xFF8C9199),
    outlineVariant = Color(0xFF42474E),
    surfaceContainerLowest = Color(0xFF0C0F13),
    surfaceContainerLow = Color(0xFF191C20),
    surfaceContainer = Color(0xFF1D2024),
    surfaceContainerHigh = Color(0xFF282A2F),
    surfaceContainerHighest = Color(0xFF33353A),
)

/** Tema Material 3 con los colores de la marca (claro y oscuro según el sistema). */
@Composable
fun PrivacyShieldTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
