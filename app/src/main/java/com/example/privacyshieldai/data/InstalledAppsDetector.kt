package com.example.privacyshieldai.data

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build

/**
 * Detecta qué apps de las plataformas están instaladas en el móvil.
 *
 * No usa QUERY_ALL_PACKAGES: solo puede ver los paquetes declarados en <queries>
 * del AndroidManifest (visibilidad de paquetes de Android 11+). La información
 * no sale del dispositivo.
 */
class InstalledAppsDetector(private val packageManager: PackageManager) {

    /** Devuelve id de plataforma -> paquete instalado (el primero de su lista que esté instalado y activo). */
    fun detect(platforms: List<Platform>): Map<String, String> =
        platforms.mapNotNull { platform ->
            platform.packages.firstOrNull { isInstalledAndEnabled(it) }?.let { platform.id to it }
        }.toMap()

    private fun isInstalledAndEnabled(packageName: String): Boolean {
        val info: ApplicationInfo = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getApplicationInfo(packageName, PackageManager.ApplicationInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                packageManager.getApplicationInfo(packageName, 0)
            }
        } catch (e: PackageManager.NameNotFoundException) {
            return false
        }
        // Algunos móviles traen Facebook preinstalada pero desactivada: no cuenta como instalada.
        return info.enabled
    }
}
