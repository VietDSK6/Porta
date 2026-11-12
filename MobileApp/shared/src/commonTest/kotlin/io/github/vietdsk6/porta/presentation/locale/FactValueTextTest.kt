package io.github.vietdsk6.porta.presentation.locale

import io.github.vietdsk6.porta.domain.model.FactValue
import kotlin.test.Test
import kotlin.test.assertEquals

class FactValueTextTest {
    @Test
    fun `whole person values do not render as decimal measurements`() {
        val previous = AppLanguageState.selected
        try {
            AppLanguageState.select("en")
            assertEquals("20 years", FactValue.NumberValue(20.0, "years").displayValue())
            assertEquals("18 months", FactValue.NumberValue(18.0, "months").displayValue())
        } finally {
            AppLanguageState.select(previous)
        }
    }

    @Test
    fun `academic scales keep their meaningful decimal notation`() {
        val previous = AppLanguageState.selected
        try {
            AppLanguageState.select("en")
            assertEquals("7.0 band", FactValue.NumberValue(7.0, "band").displayValue())
            assertEquals("3.6 / 4.0", FactValue.NumberValue(3.6, "4.0 scale").displayValue())
        } finally {
            AppLanguageState.select(previous)
        }
    }
}
