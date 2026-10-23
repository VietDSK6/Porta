package io.github.vietdsk6.porta.util.time

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

/** Supplies the user's current local date while allowing deterministic dates in tests. */
class CurrentLocalDateProvider(
    private val dateFactory: () -> LocalDate = {
        Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date
    },
) {
    fun today(): LocalDate = dateFactory()
}
