package com.example.privacyshieldai.domain

import com.example.privacyshieldai.data.Platform
import java.time.LocalDate
import java.time.format.DateTimeParseException

/** Estado de una tarea para el usuario. El orden define la prioridad (Revisar es lo más urgente). */
enum class PlatformStatus {
    /** Estaba hecha, pero la plataforma ha cambiado desde entonces. */
    REVIEW,

    PENDING,

    DONE,
}

/** Las dos tareas que se pueden completar en cada plataforma. */
enum class Task {
    /** Impedir que usen tus datos para entrenar IA. */
    TRAINING,

    /** Desactivar u ocultar las funciones de IA. */
    FEATURES,
}

/**
 * Calcula el estado a partir de la fecha `updatedOn` guardada al marcar la tarea como hecha.
 * Función pura: se prueba en PlatformStatusTest.
 */
fun computeStatus(updatedOn: String, doneForVersion: String?): PlatformStatus {
    if (doneForVersion == null) return PlatformStatus.PENDING
    return try {
        val current = LocalDate.parse(updatedOn)
        val marked = LocalDate.parse(doneForVersion)
        if (current.isAfter(marked)) PlatformStatus.REVIEW else PlatformStatus.DONE
    } catch (e: DateTimeParseException) {
        PlatformStatus.REVIEW // ante datos raros, mejor pedir que lo revise
    }
}

/** Estado de la tarea de entrenamiento de una plataforma. */
fun computeStatus(platform: Platform, doneForVersion: String?): PlatformStatus =
    computeStatus(platform.updatedOn, doneForVersion)

/** Estado global de una plataforma: el más urgente de sus tareas. */
fun overallStatus(statuses: List<PlatformStatus>): PlatformStatus =
    statuses.minByOrNull { it.ordinal } ?: PlatformStatus.PENDING
