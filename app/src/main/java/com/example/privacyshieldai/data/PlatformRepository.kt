package com.example.privacyshieldai.data

import android.content.Context
import android.util.Log
import java.io.File
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/** Resultado de comprobar si hay un catálogo remoto más nuevo. */
sealed interface RefreshResult {
    data class Updated(val catalog: PlatformCatalog) : RefreshResult
    data object UpToDate : RefreshResult
    data object NotConfigured : RefreshResult
    data class Failed(val error: Throwable) : RefreshResult
}

/**
 * Fuente única del catálogo de plataformas.
 * Orden de prioridad: copia descargada (si es más nueva) > copia incluida en la app (assets).
 */
class PlatformRepository(
    private val context: Context,
    private val remoteUrl: String?,
) {
    private val cacheFile = File(context.filesDir, CACHE_FILE)
    private val mutex = Mutex()

    private val _catalog = MutableStateFlow<PlatformCatalog?>(null)
    val catalog: StateFlow<PlatformCatalog?> = _catalog.asStateFlow()

    /** Carga el catálogo local (caché o assets). Idempotente. */
    suspend fun load(): PlatformCatalog = mutex.withLock {
        _catalog.value?.let { return@withLock it }
        withContext(Dispatchers.IO) {
            val bundled = CatalogParser.parse(
                context.assets.open(ASSET_FILE).bufferedReader(Charsets.UTF_8).use { it.readText() }
            )
            val cached = if (cacheFile.exists()) {
                runCatching { CatalogParser.parse(cacheFile.readText(Charsets.UTF_8)) }
                    .onFailure { Log.w(TAG, "Caché no válida, se ignora", it) }
                    .getOrNull()
            } else {
                null
            }
            val best = if (cached != null && CatalogParser.isNewer(cached, bundled)) cached else bundled
            _catalog.value = best
            best
        }
    }

    /** Descarga el catálogo remoto y lo guarda si es más reciente que el actual. */
    suspend fun refresh(): RefreshResult {
        if (remoteUrl.isNullOrBlank()) return RefreshResult.NotConfigured
        load()
        return try {
            val remote = withContext(Dispatchers.IO) { CatalogParser.parse(RemoteSource.fetch(remoteUrl)) }
            mutex.withLock {
                if (CatalogParser.isNewer(remote, _catalog.value)) {
                    withContext(Dispatchers.IO) { saveCache(remote) }
                    _catalog.value = remote
                    RefreshResult.Updated(remote)
                } else {
                    RefreshResult.UpToDate
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "No se pudo actualizar el catálogo", e)
            RefreshResult.Failed(e)
        }
    }

    /** Escritura atómica: primero a un temporal y luego se renombra. */
    private fun saveCache(catalog: PlatformCatalog) {
        val text = CatalogParser.serialize(catalog)
        val tmp = File(context.filesDir, "$CACHE_FILE.tmp")
        tmp.writeText(text, Charsets.UTF_8)
        if (!tmp.renameTo(cacheFile)) {
            cacheFile.delete()
            tmp.renameTo(cacheFile)
        }
    }

    private companion object {
        const val TAG = "PlatformRepository"
        const val ASSET_FILE = "plataformas.json"
        const val CACHE_FILE = "plataformas_cache.json"
    }
}
