package com.example.privacyshieldai.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.privacyshieldai.R
import com.example.privacyshieldai.domain.PlatformStatus

/** Colores de fondo y texto de una etiqueta de estado. */
private data class BadgeColors(val container: Color, val content: Color)

/*
 * Colores semánticos de estado, distintos de la paleta azul de la marca:
 * verde = Hecho, ámbar = Pendiente, rojo = Revisar. Contraste de texto AA en claro y oscuro.
 */
private val DoneLight = BadgeColors(Color(0xFFC8EFD0), Color(0xFF0B5D1E))
private val PendingLight = BadgeColors(Color(0xFFFFE7A3), Color(0xFF3D2E00))
private val ReviewLight = BadgeColors(Color(0xFFFFD7D5), Color(0xFF9B1C1C))

private val DoneDark = BadgeColors(Color(0xFF1E4D2B), Color(0xFFB5F2C3))
private val PendingDark = BadgeColors(Color(0xFF5C4300), Color(0xFFFFE7A3))
private val ReviewDark = BadgeColors(Color(0xFF6B1D1D), Color(0xFFFFD7D5))

@Composable
fun StatusBadge(status: PlatformStatus, modifier: Modifier = Modifier) {
    // Se deduce del tema activo para seguir siempre a PrivacyShieldTheme.
    val dark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val colors = when (status) {
        PlatformStatus.DONE -> if (dark) DoneDark else DoneLight
        PlatformStatus.PENDING -> if (dark) PendingDark else PendingLight
        PlatformStatus.REVIEW -> if (dark) ReviewDark else ReviewLight
    }
    // Símbolo además del color, para quien no distingue bien los colores.
    val (symbol, label) = when (status) {
        PlatformStatus.DONE -> "✓" to R.string.status_done
        PlatformStatus.PENDING -> "•" to R.string.status_pending
        PlatformStatus.REVIEW -> "!" to R.string.status_review
    }
    Surface(
        modifier = modifier,
        color = colors.container,
        contentColor = colors.content,
        shape = MaterialTheme.shapes.small,
    ) {
        Text(
            text = "$symbol ${stringResource(label)}",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}
