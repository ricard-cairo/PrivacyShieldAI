package com.example.privacyshieldai.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.privacyshieldai.PrivacyShieldApp
import com.example.privacyshieldai.data.InstalledAppsDetector
import com.example.privacyshieldai.data.Platform
import com.example.privacyshieldai.data.PlatformRepository
import com.example.privacyshieldai.data.ProgressStore
import com.example.privacyshieldai.data.RefreshResult
import com.example.privacyshieldai.domain.PlatformStatus
import com.example.privacyshieldai.domain.Task
import com.example.privacyshieldai.domain.computeStatus
import com.example.privacyshieldai.domain.overallStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class PlatformItem(
    val platform: Platform,
    /** Estado de la tarea "Entrenamiento". */
    val trainingStatus: PlatformStatus,
    /** Estado de la tarea "Funciones de IA", o null si la plataforma no tiene guía. */
    val featuresStatus: PlatformStatus? = null,
    /** Paquete de la app instalada en el móvil, o null si no está instalada. */
    val installedPackage: String? = null,
) {
    val isInstalled: Boolean get() = installedPackage != null

    /** Estado global (el más urgente de sus tareas). Es el que se muestra en la lista. */
    val status: PlatformStatus = overallStatus(listOfNotNull(trainingStatus, featuresStatus))

    fun statusOf(task: Task): PlatformStatus? = when (task) {
        Task.TRAINING -> trainingStatus
        Task.FEATURES -> featuresStatus
    }
}

/** Mensajes puntuales para la UI (el texto vive en strings.xml). */
enum class UserMessage { UPDATED, UP_TO_DATE, FAILED, NOT_CONFIGURED }

data class UiState(
    val loading: Boolean = true,
    val items: List<PlatformItem> = emptyList(),
    val dataVersion: String? = null,
    val refreshing: Boolean = false,
    val message: UserMessage? = null,
) {
    /** Tareas completadas / totales (entrenamiento + funciones de IA). */
    val doneTasks: Int = items.count { it.trainingStatus == PlatformStatus.DONE } +
        items.count { it.featuresStatus == PlatformStatus.DONE }
    val trainingDone: Int = items.count { it.trainingStatus == PlatformStatus.DONE }
    val featuresDone: Int = items.count { it.featuresStatus == PlatformStatus.DONE }
    val featuresTotal: Int = items.count { it.featuresStatus != null }
    val totalTasks: Int = items.size + featuresTotal
    val installedItems: List<PlatformItem> = items.filter { it.isInstalled }
    val otherItems: List<PlatformItem> = items.filterNot { it.isInstalled }
}

class MainViewModel(
    private val repository: PlatformRepository,
    private val progressStore: ProgressStore,
    private val installedApps: InstalledAppsDetector,
) : ViewModel() {

    /** id de plataforma -> paquete instalado. Se recalcula al volver a la app (onResume). */
    private val installed = MutableStateFlow<Map<String, String>>(emptyMap())
    private val refreshing = MutableStateFlow(false)
    private val message = MutableStateFlow<UserMessage?>(null)
    private var detectJob: Job? = null

    val uiState: StateFlow<UiState> = combine(
        repository.catalog,
        progressStore.progress,
        installed,
        refreshing,
        message,
    ) { catalog, progress, installedMap, isRefreshing, msg ->
        if (catalog == null) {
            UiState(loading = true)
        } else {
            UiState(
                loading = false,
                items = catalog.platforms
                    .map { platform ->
                        PlatformItem(
                            platform = platform,
                            trainingStatus = computeStatus(platform.updatedOn, progress.training[platform.id]),
                            featuresStatus = platform.aiFeatures?.let {
                                computeStatus(it.updatedOn, progress.features[platform.id])
                            },
                            installedPackage = installedMap[platform.id],
                        )
                    }
                    .sortedBy { it.status.ordinal }, // Revisar > Pendiente > Hecho; estable dentro de cada grupo
                dataVersion = catalog.dataVersion,
                refreshing = isRefreshing,
                message = msg,
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState())

    init {
        viewModelScope.launch {
            repository.load()
            refreshInstalledApps()
            refresh(userInitiated = false)
        }
    }

    /** Vuelve a detectar las apps instaladas (el usuario puede instalar o desinstalar con la app en segundo plano). */
    fun refreshInstalledApps() {
        detectJob?.cancel() // solo cuenta la detección más reciente
        detectJob = viewModelScope.launch {
            val catalog = repository.load()
            // PackageManager hace llamadas entre procesos: mejor fuera del hilo principal.
            installed.value = withContext(Dispatchers.IO) { installedApps.detect(catalog.platforms) }
        }
    }

    fun refresh(userInitiated: Boolean = true) {
        if (refreshing.value) return
        viewModelScope.launch {
            refreshing.value = true
            val result = repository.refresh()
            refreshing.value = false
            if (result is RefreshResult.Updated) refreshInstalledApps() // el catálogo nuevo puede traer otros paquetes
            message.value = when (result) {
                is RefreshResult.Updated -> UserMessage.UPDATED
                RefreshResult.UpToDate -> if (userInitiated) UserMessage.UP_TO_DATE else null
                RefreshResult.NotConfigured -> if (userInitiated) UserMessage.NOT_CONFIGURED else null
                is RefreshResult.Failed -> if (userInitiated) UserMessage.FAILED else null
            }
        }
    }

    fun setDone(platform: Platform, task: Task, done: Boolean) {
        val updatedOn = when (task) {
            Task.TRAINING -> platform.updatedOn
            Task.FEATURES -> platform.aiFeatures?.updatedOn ?: return
        }
        viewModelScope.launch {
            if (done) {
                progressStore.markDone(task, platform.id, updatedOn)
            } else {
                progressStore.markPending(task, platform.id)
            }
        }
    }

    fun messageShown() {
        message.value = null
    }

    companion object {
        fun factory(app: PrivacyShieldApp): ViewModelProvider.Factory = viewModelFactory {
            initializer { MainViewModel(app.repository, app.progressStore, app.installedApps) }
        }
    }
}
