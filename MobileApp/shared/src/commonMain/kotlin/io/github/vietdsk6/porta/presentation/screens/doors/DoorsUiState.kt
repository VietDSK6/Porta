package io.github.vietdsk6.porta.presentation.screens.doors

import io.github.vietdsk6.porta.domain.model.EligibilityState
import io.github.vietdsk6.porta.domain.model.OpportunityCategory

data class DoorsUiState(
    val isSampleMode: Boolean = true,
    val isSampleCatalog: Boolean = true,
    val catalogDoorCount: Int = 0,
    val selectedFilter: DoorFilter = DoorFilter.ALL,
    val searchQuery: String = "",
    val selectedCategory: OpportunityCategory? = null,
    val selectedDeadlineFilter: DoorDeadlineFilter = DoorDeadlineFilter.ALL,
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
    val category: OpportunityCategory = OpportunityCategory.SCHOLARSHIP,
    val categoryLabel: String = "",
    val state: EligibilityState,
    val stateLabel: String,
    val deadlineLabel: String,
    val checkedLabel: String,
    val isArchived: Boolean = false,
    val sourceReviewNeedsAttention: Boolean = false,
    val isSaved: Boolean,
    val primaryReason: String,
    val deadlineBucket: DoorDeadlineBucket = DoorDeadlineBucket.UPCOMING,
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

enum class DoorDeadlineFilter(val label: String) {
    ALL("Any deadline"),
    UPCOMING("Upcoming"),
    TBA_OR_MISSING("TBA or missing"),
    CLOSED("Closed"),
}

enum class DoorDeadlineBucket {
    UPCOMING,
    TBA_OR_MISSING,
    CLOSED,
}

sealed interface DoorsUiEvent {
    data class FilterChanged(val filter: DoorFilter) : DoorsUiEvent

    data class SearchChanged(val query: String) : DoorsUiEvent

    data class CategoryChanged(val category: OpportunityCategory?) : DoorsUiEvent

    data class DeadlineFilterChanged(val filter: DoorDeadlineFilter) : DoorsUiEvent

    data class SavedChanged(val opportunityId: String) : DoorsUiEvent

    data object DismissHighlights : DoorsUiEvent
}
