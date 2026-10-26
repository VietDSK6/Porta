package io.github.vietdsk6.porta.util.time

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CurrentLocalDateProviderTest {
    @Test
    fun `refresh emits only when local calendar day changes`() {
        var clockDate = LocalDate(2026, 10, 20)
        val provider = CurrentLocalDateProvider { clockDate }

        assertFalse(provider.refresh())
        clockDate = LocalDate(2026, 10, 21)

        assertTrue(provider.refresh())
        assertEquals(clockDate, provider.currentDate.value)
        assertFalse(provider.refresh())
    }
}
