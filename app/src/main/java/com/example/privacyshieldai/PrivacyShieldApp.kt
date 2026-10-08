package com.example.privacyshieldai

import android.app.Application
import com.example.privacyshieldai.data.InstalledAppsDetector
import com.example.privacyshieldai.data.PlatformRepository
import com.example.privacyshieldai.data.ProgressStore
import com.example.privacyshieldai.work.Notifications
import com.example.privacyshieldai.work.UpdateCheckWorker

/** Contenedor sencillo de dependencias (sin Hilt para mantener el proyecto ligero). */
class PrivacyShieldApp : Application() {

    lateinit var repository: PlatformRepository
        private set
    lateinit var progressStore: ProgressStore
        private set
    lateinit var installedApps: InstalledAppsDetector
        private set

    override fun onCreate() {
        super.onCreate()
        repository = PlatformRepository(this, AppConfig.REMOTE_CATALOG_URL)
        progressStore = ProgressStore(this)
        installedApps = InstalledAppsDetector(packageManager)
        Notifications.createChannel(this)
        if (!AppConfig.REMOTE_CATALOG_URL.isNullOrBlank()) {
            UpdateCheckWorker.schedule(this)
        }
    }
}
