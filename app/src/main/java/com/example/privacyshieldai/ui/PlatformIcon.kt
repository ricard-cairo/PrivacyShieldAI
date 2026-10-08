package com.example.privacyshieldai.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Icono de la plataforma.
 * - Si la app está instalada, se usa su icono real, leído del propio móvil (no se incluyen logos en la app).
 * - Si no, un círculo con la inicial del nombre.
 */
@Composable
fun PlatformIcon(
    item: PlatformItem,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
) {
    val context = LocalContext.current
    val sizePx = with(LocalDensity.current) { size.roundToPx() }
    val packageName = item.installedPackage

    val icon: ImageBitmap? by produceState<ImageBitmap?>(initialValue = null, packageName, sizePx) {
        value = if (packageName == null) {
            null
        } else {
            withContext(Dispatchers.IO) {
                // null si se desinstaló mientras tanto o el móvil no devuelve el icono
                runCatching {
                    context.packageManager.getApplicationIcon(packageName)
                        .toBitmap(width = sizePx, height = sizePx)
                        .asImageBitmap()
                }.getOrNull()
            }
        }
    }

    val bitmap = icon
    if (bitmap != null) {
        Image(
            bitmap = bitmap,
            contentDescription = null, // el nombre ya aparece al lado
            modifier = modifier.size(size),
        )
    } else {
        Surface(
            modifier = modifier.size(size),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = item.platform.name.take(1).uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
    }
}
