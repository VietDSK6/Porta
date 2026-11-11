package io.github.vietdsk6.porta.presentation.screens.route

import io.github.vietdsk6.porta.domain.model.CostBand
import io.github.vietdsk6.porta.domain.model.EffortLevel

data class RouteUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val canSave: Boolean = false,
    val horizonDays: Int = 180,
    val maxTotalCost: CostBand = CostBand.UNKNOWN,
    val maxEffort: EffortLevel = EffortLevel.VERY_HIGH,
    val routes: List<RouteOptionUiState> = emptyList(),
    val selectedRouteId: String? = null,
    val error: String? = null,
    val saveNotice: String? = null,
) {
    val selectedRoute: RouteOptionUiState?
        get() = routes.firstOrNull { it.id == selectedRouteId } ?: routes.firstOrNull()
}

data class RouteOptionUiState(
    val id: String,
    val steps: List<RouteStepUiState>,
    val openedTotal: Int,
    val openedSaved: Int,
    val daysToFirstDoor: Int?,
    val totalDays: Int,
    val costRankSum: Int,
    val explanation: String,
) {
    val summary: String
        get() = buildString {
            val improvedDoorCount = steps.flatMap(RouteStepUiState::doorsImproved).distinct().size
            val lostDoorCount = steps.flatMap(RouteStepUiState::doorsLost).distinct().size
            if (openedTotal > 0) {
                append("$openedTotal doors would pass their assessed checks")
                if (openedSaved > 0) append(", including $openedSaved saved")
            } else {
                append("No door passes every assessed check yet; checks for $improvedDoorCount doors would improve")
            }
            append(" across ${steps.size} steps and $totalDays days.")
            if (lostDoorCount > 0) append(" $lostDoorCount would close before this route finishes.")
            append(" Official requirements and deadlines still need checking.")
        }
}

data class RouteStepUiState(
    val id: String,
    val title: String,
    val startDateLabel: String,
    val endDateLabel: String,
    val durationDays: Int,
    val doorsOpened: List<String>,
    val doorsImproved: List<String>,
    val doorsLost: List<String>,
    val effort: EffortLevel,
    val cost: CostBand,
)

sealed interface RouteUiEvent {
    data class HorizonChanged(val days: Int) : RouteUiEvent

    data class MaxCostChanged(val cost: CostBand) : RouteUiEvent

    data class MaxEffortChanged(val effort: EffortLevel) : RouteUiEvent

    data class RouteSelected(val id: String) : RouteUiEvent

    data object SaveRoute : RouteUiEvent

    data object NoticeDismissed : RouteUiEvent
}
