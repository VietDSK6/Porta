package io.github.vietdsk6.porta.presentation.locale

import io.github.vietdsk6.porta.domain.model.FactValue
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class LocalizedValueTextTest {
    @Test
    fun `Vietnamese interface formats fact values and dates without changing domain values`() {
        val previous = AppLanguageState.selected
        try {
            AppLanguageState.select("vi")
            assertEquals("Có", FactValue.BooleanValue(true).displayValue())
            assertEquals("Không", FactValue.BooleanValue(false).displayValue())
            assertEquals("18/03/2005", formatLocalDate(LocalDate(2005, 3, 18)))
        } finally {
            AppLanguageState.select(previous)
        }
    }

    @Test
    fun `English interface formats dates month first`() {
        val previous = AppLanguageState.selected
        try {
            AppLanguageState.select("en")
            assertEquals("Yes", FactValue.BooleanValue(true).displayValue())
            assertEquals("03/18/2005", formatLocalDate(LocalDate(2005, 3, 18)))
        } finally {
            AppLanguageState.select(previous)
        }
    }
}
