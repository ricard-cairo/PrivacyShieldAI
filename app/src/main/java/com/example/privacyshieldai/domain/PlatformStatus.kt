package com.example.privacyshieldai.domain

import com.example.privacyshieldai.data.Platform
import java.time.LocalDate
import java.time.format.DateTimeParseException

/** Estado de una plataforma para el usuario. El orden define cómo se ordena la lista. */
enum class PlatformStatus {
    /** Estaba hecha, pero la plataforma ha cambiado desde entonces. */
    REVIEW,

    PENDING,

    DONE,
}

/**
 * Calcula el estado a partir de la fecha `updatedOn` guardada al marcarla como hecha.
 * Función pura: se prueba en PlatformStatusTest.
 */
fun computeStatus(platform: Platform, doneForVersion: String?): PlatformStatus {
    if (doneForVersion == null) return PlatformStatus.PENDING
    return try {
        val current = LocalDate.parse(platform.updatedOn)
        val marked = LocalDate.parse(doneForVersion)
        if (current.isAfter(marked)) PlatformStatus.REVIEW else PlatformStatus.DONE
    } catch (e: DateTimeParseException) {
        PlatformStatus.REVIEW // ante datos raros, mejor pedir que lo revise
    }
}
