package com.example.privacyshieldai.data

import kotlinx.serialization.Serializable

/** Catálogo completo de plataformas. Se lee de assets/plataformas.json o de la copia remota. */
@Serializable
data class PlatformCatalog(
    val schemaVersion: Int,
    /** Fecha ISO (AAAA-MM-DD) de la última revisión del catálogo. */
    val dataVersion: String,
    val platforms: List<Platform>,
)

/** Cómo se desactiva el uso de datos para IA en una plataforma. */
@Serializable
enum class Mechanism {
    /** Interruptor en los ajustes de la cuenta. */
    TOGGLE,

    /** Formulario web de objeción. */
    FORM,

    /** Solicitud por correo (p. ej. RGPD). */
    REQUEST,

    /** No existe opción; solo recomendaciones. */
    NONE,
}

@Serializable
data class Platform(
    val id: String,
    val name: String,
    val category: String,
    val mechanism: Mechanism,
    /**
     * Paquetes Android de la plataforma, por orden de preferencia (p. ej. app principal y versión Lite).
     * Cada uno debe estar declarado en <queries> del AndroidManifest (lo comprueba CatalogTest).
     */
    val packages: List<String> = emptyList(),
    val regions: List<String> = emptyList(),
    val summary: String,
    val steps: List<String>,
    val url: String? = null,
    val extraUrl: String? = null,
    val extraUrlLabel: String? = null,
    val email: String? = null,
    /** false si el enlace directo no se ha comprobado en una fuente oficial. */
    val linkVerified: Boolean = false,
    val notes: String? = null,
    val sourceUrl: String? = null,
    /** Fecha ISO del último cambio de instrucciones o de política de esta plataforma. */
    val updatedOn: String,
    /** Guía para desactivar u ocultar las funciones de IA de la plataforma (segunda tarea). */
    val aiFeatures: AiFeatures? = null,
)

/** Hasta qué punto se pueden desactivar las funciones de IA de una plataforma. */
@Serializable
enum class FeatureMechanism {
    /** Hay un interruptor real. */
    TOGGLE,

    /** Solo se pueden desactivar algunas funciones u ocultarlas/silenciarlas. */
    PARTIAL,

    /** No se pueden desactivar; solo recomendaciones. */
    NONE,
}

/** Guía para desactivar las funciones de IA (asistentes, resúmenes, memoria...). Independiente del entrenamiento. */
@Serializable
data class AiFeatures(
    val mechanism: FeatureMechanism,
    val summary: String,
    val steps: List<String>,
    val url: String? = null,
    val linkVerified: Boolean = false,
    val notes: String? = null,
    val sourceUrl: String? = null,
    /** Igual que Platform.updatedOn, pero para esta tarea: si cambia, la tarea pasa a "Revisar". */
    val updatedOn: String,
)
