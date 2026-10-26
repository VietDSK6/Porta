package io.github.vietdsk6.porta.presentation.screens.doors

import io.github.vietdsk6.porta.domain.model.EligibilityState

data class DoorsUiState(
    val isSampleMode: Boolean = true,
    val isSampleCatalog: Boolean = true,
    val catalogDoorCount: Int = 0,
    val selectedFilter: DoorFilter = DoorFilter.ALL,
    val doors: List<DoorCardUiState> = emptyList(),
    val openCount: Int = 0,
    val gapCount: Int = 0,
    val needsInfoCount: Int = 0,
    val notAvailableCount: Int = 0,
    val closedCount: Int = 0,
    val archivedCount: Int = 0,
    val sourceReviewDueCount: Int = 0,
    val highlightedDoorIds: Set<String> = emptySet(),
)

data class DoorCardUiState(
    val id: String,
    val title: String,
    val provider: String,
    val categoryLabel: String = "",
    val state: EligibilityState,
    val stateLabel: String,
    val deadlineLabel: String,
    val checkedLabel: String,
    val isArchived: Boolean = false,
    val sourceReviewNeedsAttention: Boolean = false,
    val isSaved: Boolean,
    val primaryReason: String,
)

enum class DoorFilter(val label: String) {
    ALL("ALL"),
    OPEN("ASSESSED RULES PASSED"),
    GAPS("NEEDS ACTION"),
    VERIFY("NEEDS INFORMATION"),
    NOT_AVAILABLE("NOT AVAILABLE THIS CYCLE"),
    CLOSED("CLOSED"),
    ARCHIVED("ARCHIVED CYCLES"),
    SAVED("SAVED"),
}

sealed interface DoorsUiEvent {
    data class FilterChanged(val filter: DoorFilter) : DoorsUiEvent

    data class SavedChanged(val opportunityId: String) : DoorsUiEvent

    data object DismissHighlights : DoorsUiEvent
}
