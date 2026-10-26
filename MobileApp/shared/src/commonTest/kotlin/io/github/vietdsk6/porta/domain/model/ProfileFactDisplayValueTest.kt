package io.github.vietdsk6.porta.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class ProfileFactDisplayValueTest {
    @Test
    fun `whole person values do not render as decimal measurements`() {
        assertEquals("20 years", FactValue.NumberValue(20.0, "years").displayValue())
        assertEquals("18 months", FactValue.NumberValue(18.0, "months").displayValue())
    }

    @Test
    fun `academic scales keep their meaningful decimal notation`() {
        assertEquals("7.0 band", FactValue.NumberValue(7.0, "band").displayValue())
        assertEquals("3.6 / 4.0", FactValue.NumberValue(3.6, "4.0 scale").displayValue())
    }
}
