package com.example.privacyshieldai.work

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.privacyshieldai.PrivacyShieldApp
import com.example.privacyshieldai.data.PlatformCatalog
import com.example.privacyshieldai.data.RefreshResult
import com.example.privacyshieldai.domain.PlatformStatus
import com.example.privacyshieldai.domain.computeStatus
import java.io.IOException
import java.util.concurrent.TimeUnit

/** Comprueba una vez al día si hay catálogo nuevo y avisa si alguna plataforma hecha ha cambiado. */
class UpdateCheckWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as PrivacyShieldApp
        return when (val result = app.repository.refresh()) {
            is RefreshResult.Updated -> {
                notifyIfNeeded(app, result.catalog)
                Result.success()
            }
            RefreshResult.UpToDate -> {
                // La app pudo descargar el catálogo al abrirse; avisar igualmente si no se ha hecho.
                app.repository.catalog.value?.let { notifyIfNeeded(app, it) }
                Result.success()
            }
            // Reintentar solo errores de red; un JSON roto no se arregla reintentando.
            is RefreshResult.Failed -> if (result.error is IOException) Result.retry() else Result.success()
            RefreshResult.NotConfigured -> Result.success()
        }
    }

    private suspend fun notifyIfNeeded(app: PrivacyShieldApp, catalog: PlatformCatalog) {
        val store = app.progressStore
        if (store.lastNotifiedVersion() == catalog.dataVersion) return
        val progress = store.snapshot()
        val changed = catalog.platforms
            .filter { computeStatus(it, progress[it.id]) == PlatformStatus.REVIEW }
            .map { it.name }
        if (changed.isNotEmpty()) {
            Notifications.showReviewNeeded(app, changed)
            store.setLastNotifiedVersion(catalog.dataVersion)
        }
    }

    companion object {
        private const val WORK_NAME = "catalog-update-check"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<UpdateCheckWorker>(1, TimeUnit.DAYS)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request,
            )
        }
    }
}
