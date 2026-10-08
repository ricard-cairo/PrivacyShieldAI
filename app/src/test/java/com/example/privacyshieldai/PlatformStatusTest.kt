package com.example.privacyshieldai

import com.example.privacyshieldai.data.Mechanism
import com.example.privacyshieldai.data.Platform
import com.example.privacyshieldai.domain.PlatformStatus
import com.example.privacyshieldai.domain.computeStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class PlatformStatusTest {

    private fun platform(updatedOn: String) = Platform(
        id = "test",
        name = "Test",
        category = "Test",
        mechanism = Mechanism.TOGGLE,
        summary = "",
        steps = listOf("Paso"),
        updatedOn = updatedOn,
    )

    @Test
    fun notMarkedIsPending() {
        assertEquals(PlatformStatus.PENDING, computeStatus(platform("2026-09-30"), null))
    }

    @Test
    fun markedWithSameVersionIsDone() {
        assertEquals(PlatformStatus.DONE, computeStatus(platform("2026-09-30"), "2026-09-30"))
    }

    @Test
    fun platformChangedAfterMarkingNeedsReview() {
        assertEquals(PlatformStatus.REVIEW, computeStatus(platform("2026-11-15"), "2026-09-30"))
    }

    @Test
    fun invalidStoredDateNeedsReview() {
        assertEquals(PlatformStatus.REVIEW, computeStatus(platform("2026-09-30"), "no-es-fecha"))
    }
}
