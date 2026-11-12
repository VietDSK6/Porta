@file:OptIn(ExperimentalTime::class)

package io.github.vietdsk6.porta.presentation.locale

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/** Display only: persisted dates and pack JSON stay in ISO 8601 form. */
fun formatLocalDate(date: LocalDate): String {
    val day = date.dayOfMonth.toString().padStart(2, '0')
    val month = date.monthNumber.toString().padStart(2, '0')
    return if (AppLanguageState.effectiveLanguageTag.startsWith("vi")) {
        "$day/$month/${date.year}"
    } else {
        "$month/$day/${date.year}"
    }
}

fun Long.asLocalizedDate(timeZone: TimeZone = TimeZone.currentSystemDefault()): String = formatLocalDate(Instant.fromEpochMilliseconds(this).toLocalDateTime(timeZone).date)
