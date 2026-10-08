package com.example.privacyshieldai.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.progressDataStore: DataStore<Preferences> by preferencesDataStore(name = "progress")

/**
 * Progreso del usuario, guardado solo en el dispositivo.
 * Por cada plataforma marcada como hecha se guarda el valor de `updatedOn` de ese momento;
 * si el catálogo trae una fecha posterior, la plataforma pasa a "Revisar".
 */
class ProgressStore(context: Context) {

    private val dataStore = context.applicationContext.progressDataStore

    /** Mapa id de plataforma -> `updatedOn` con el que se marcó como hecha. */
    val progress: Flow<Map<String, String>> = dataStore.data.map { prefs ->
        prefs.asMap().entries.mapNotNull { (key, value) ->
            if (key.name.startsWith(DONE_PREFIX) && value is String) {
                key.name.removePrefix(DONE_PREFIX) to value
            } else {
                null
            }
        }.toMap()
    }

    suspend fun snapshot(): Map<String, String> = progress.first()

    suspend fun markDone(platform: Platform) {
        dataStore.edit { it[doneKey(platform.id)] = platform.updatedOn }
    }

    suspend fun markPending(platformId: String) {
        dataStore.edit { it.remove(doneKey(platformId)) }
    }

    /** Última versión del catálogo por la que ya se envió un aviso (evita avisos repetidos). */
    suspend fun lastNotifiedVersion(): String? = dataStore.data.first()[LAST_NOTIFIED]

    suspend fun setLastNotifiedVersion(version: String) {
        dataStore.edit { it[LAST_NOTIFIED] = version }
    }

    private fun doneKey(platformId: String) = stringPreferencesKey(DONE_PREFIX + platformId)

    private companion object {
        const val DONE_PREFIX = "done_"
        val LAST_NOTIFIED = stringPreferencesKey("last_notified_version")
    }
}
