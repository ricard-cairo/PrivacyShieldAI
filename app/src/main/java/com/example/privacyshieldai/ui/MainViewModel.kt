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
import com.example.privacyshieldai.domain.computeStatus
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
    val status: PlatformStatus,
    /** Paquete de la app instalada en el móvil, o null si no está instalada. */
    val installedPackage: String? = null,
) {
    val isInstalled: Boolean get() = installedPackage != null
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
    val doneCount: Int = items.count { it.status == PlatformStatus.DONE }
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
                    .map { PlatformItem(it, computeStatus(it, progress[it.id]), installedMap[it.id]) }
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

    fun setDone(platform: Platform, done: Boolean) {
        viewModelScope.launch {
            if (done) progressStore.markDone(platform) else progressStore.markPending(platform.id)
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
