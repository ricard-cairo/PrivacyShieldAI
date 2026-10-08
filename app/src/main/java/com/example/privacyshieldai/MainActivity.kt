package com.example.privacyshieldai

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.privacyshieldai.ui.MainViewModel
import com.example.privacyshieldai.ui.PlatformDetailScreen
import com.example.privacyshieldai.ui.PlatformListScreen
import com.example.privacyshieldai.ui.theme.PrivacyShieldTheme
import com.example.privacyshieldai.work.Notifications

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as PrivacyShieldApp

        setContent {
            PrivacyShieldTheme {
                val viewModel: MainViewModel = viewModel(factory = MainViewModel.factory(app))
                val state by viewModel.uiState.collectAsStateWithLifecycle()
                var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
                val context = LocalContext.current

                // Cada vez que se vuelve a la app, se comprueba qué plataformas están instaladas.
                LifecycleResumeEffect(Unit) {
                    viewModel.refreshInstalledApps()
                    onPauseOrDispose { /* nada que liberar */ }
                }

                // El permiso de notificaciones se pide cuando tiene sentido:
                // al marcar una plataforma como hecha (para avisar si cambia).
                val notificationPermission = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { /* si lo deniega, la app funciona igual sin avisos */ }

                val selected = state.items.firstOrNull { it.platform.id == selectedId }
                if (selected != null) {
                    BackHandler { selectedId = null }
                    PlatformDetailScreen(
                        item = selected,
                        onBack = { selectedId = null },
                        onSetDone = { done ->
                            viewModel.setDone(selected.platform, done)
                            if (done && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                !Notifications.canNotify(context)
                            ) {
                                notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        },
                    )
                } else {
                    PlatformListScreen(
                        state = state,
                        onOpenPlatform = { selectedId = it },
                        onRefresh = { viewModel.refresh() },
                        onMessageShown = viewModel::messageShown,
                    )
                }
            }
        }
    }
}
