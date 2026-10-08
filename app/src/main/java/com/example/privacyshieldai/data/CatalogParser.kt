package com.example.privacyshieldai.data

import java.time.LocalDate
import kotlinx.serialization.json.Json

/** Lectura y comparación de catálogos. Sin dependencias de Android para poder probarlo con tests unitarios. */
object CatalogParser {

    /** Versión de esquema más alta que entiende esta versión de la app. */
    const val SUPPORTED_SCHEMA_VERSION = 1

    private val json = Json {
        ignoreUnknownKeys = true // campos nuevos en el JSON no rompen versiones antiguas de la app
        isLenient = false
    }

    /** Convierte el texto JSON en un catálogo. Lanza excepción si el formato no es válido. */
    fun parse(text: String): PlatformCatalog {
        val catalog = json.decodeFromString(PlatformCatalog.serializer(), text)
        require(catalog.schemaVersion <= SUPPORTED_SCHEMA_VERSION) {
            "Esquema ${catalog.schemaVersion} no soportado"
        }
        LocalDate.parse(catalog.dataVersion) // valida el formato de fecha
        return catalog
    }

    fun serialize(catalog: PlatformCatalog): String =
        json.encodeToString(PlatformCatalog.serializer(), catalog)

    /** true si [candidate] es más reciente que [current]. */
    fun isNewer(candidate: PlatformCatalog, current: PlatformCatalog?): Boolean {
        if (current == null) return true
        return LocalDate.parse(candidate.dataVersion).isAfter(LocalDate.parse(current.dataVersion))
    }
}
