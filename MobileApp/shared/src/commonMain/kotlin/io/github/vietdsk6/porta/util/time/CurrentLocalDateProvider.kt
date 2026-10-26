package io.github.vietdsk6.porta.util.time

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private val _currentDate = MutableStateFlow(dateFactory())
    val currentDate: StateFlow<LocalDate> = _currentDate.asStateFlow()

    fun today(): LocalDate = _currentDate.value

    /** Re-reads the platform clock and emits only when the user's local calendar day changed. */
    fun refresh(): Boolean {
        val refreshed = dateFactory()
        if (refreshed == _currentDate.value) return false
        _currentDate.value = refreshed
        return true
    }
}
