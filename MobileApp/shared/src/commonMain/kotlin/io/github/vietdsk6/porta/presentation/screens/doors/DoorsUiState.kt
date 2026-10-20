package io.github.vietdsk6.porta.presentation.screens.doors

import io.github.vietdsk6.porta.domain.model.EligibilityState

data class DoorsUiState(
    val isSampleMode: Boolean = true,
    val selectedFilter: DoorFilter = DoorFilter.ALL,
    val doors: List<DoorCardUiState> = emptyList(),
    val openCount: Int = 0,
    val gapCount: Int = 0,
    val needsInfoCount: Int = 0,
)

data class DoorCardUiState(
    val id: String,
    val title: String,
    val provider: String,
    val state: EligibilityState,
    val deadlineLabel: String,
    val checkedLabel: String,
    val isSaved: Boolean,
    val primaryReason: String,
)

enum class DoorFilter(val label: String) {
    ALL("All"),
    OPEN("Open"),
    GAPS("Has gaps"),
    VERIFY("Verify"),
    SAVED("Saved"),
}

sealed interface DoorsUiEvent {
    data class FilterChanged(val filter: DoorFilter) : DoorsUiEvent

    data class SavedChanged(val opportunityId: String) : DoorsUiEvent
}
