package com.example.privacyshieldai

object AppConfig {
    /**
     * URL pública del catálogo: el mismo app/src/main/assets/plataformas.json de la rama main de GitHub.
     * Cada push a main con un `dataVersion` más nuevo llega a los usuarios (en la comprobación diaria,
     * al abrir la app o con el botón "Actualizar"). GitHub puede tardar unos minutos en servir la versión nueva.
     * Si es null, la app solo usa la copia incluida y no comprueba actualizaciones.
     */
    val REMOTE_CATALOG_URL: String? =
        "https://raw.githubusercontent.com/ricard-cairo/PrivacyShieldAI/main/app/src/main/assets/plataformas.json"
}
