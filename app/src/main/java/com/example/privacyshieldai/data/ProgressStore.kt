package com.example.privacyshieldai.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.privacyshieldai.domain.Task
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.progressDataStore: DataStore<Preferences> by preferencesDataStore(name = "progress")

/**
 * Progreso del usuario por tarea: id de plataforma -> `updatedOn` con el que se marcó como hecha.
 */
data class UserProgress(
    val training: Map<String, String> = emptyMap(),
    val features: Map<String, String> = emptyMap(),
)

/**
 * Progreso del usuario, guardado solo en el dispositivo.
 * Por cada tarea marcada como hecha se guarda el valor de `updatedOn` de ese momento;
 * si el catálogo trae una fecha posterior, la tarea pasa a "Revisar".
 */
class ProgressStore(context: Context) {

    private val dataStore = context.applicationContext.progressDataStore

    val progress: Flow<UserProgress> = dataStore.data.map { prefs ->
        val all = prefs.asMap().entries.mapNotNull { (key, value) ->
            if (value is String) key.name to value else null
        }
        UserProgress(
            training = all.filter { it.first.startsWith(TRAINING_PREFIX) }
                .associate { it.first.removePrefix(TRAINING_PREFIX) to it.second },
            features = all.filter { it.first.startsWith(FEATURES_PREFIX) }
                .associate { it.first.removePrefix(FEATURES_PREFIX) to it.second },
        )
    }

    suspend fun snapshot(): UserProgress = progress.first()

    /** Marca la tarea como hecha guardando la versión (`updatedOn`) de sus instrucciones. */
    suspend fun markDone(task: Task, platformId: String, updatedOn: String) {
        dataStore.edit { it[key(task, platformId)] = updatedOn }
    }

    suspend fun markPending(task: Task, platformId: String) {
        dataStore.edit { it.remove(key(task, platformId)) }
    }

    /** Última versión del catálogo por la que ya se envió un aviso (evita avisos repetidos). */
    suspend fun lastNotifiedVersion(): String? = dataStore.data.first()[LAST_NOTIFIED]

    suspend fun setLastNotifiedVersion(version: String) {
        dataStore.edit { it[LAST_NOTIFIED] = version }
    }

    private fun key(task: Task, platformId: String) = stringPreferencesKey(
        when (task) {
            Task.TRAINING -> TRAINING_PREFIX
            Task.FEATURES -> FEATURES_PREFIX
        } + platformId
    )

    private companion object {
        // "done_" se mantiene para no perder el progreso guardado por versiones anteriores.
        const val TRAINING_PREFIX = "done_"
        const val FEATURES_PREFIX = "feat_"
        val LAST_NOTIFIED = stringPreferencesKey("last_notified_version")
    }
}
