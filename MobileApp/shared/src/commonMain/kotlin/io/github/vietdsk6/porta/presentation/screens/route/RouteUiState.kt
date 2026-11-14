package io.github.vietdsk6.porta.presentation.screens.route

import io.github.vietdsk6.porta.domain.model.CostBand
import io.github.vietdsk6.porta.domain.model.EffortLevel
import io.github.vietdsk6.porta.domain.model.FactKey

data class RouteUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val canSave: Boolean = false,
    val isFictionalProfile: Boolean = false,
    val isIllustrativePreview: Boolean = false,
    val catalogUnavailable: Boolean = false,
    val focusedOpportunityId: String? = null,
    val focusedOpportunityTitle: String? = null,
    val horizonDays: Int = 240,
    val maxCostPerStep: CostBand = CostBand.UNKNOWN,
    val isRecalculating: Boolean = false,
    val maxEffort: EffortLevel = EffortLevel.VERY_HIGH,
    val routes: List<RouteOptionUiState> = emptyList(),
    val selectedRouteId: String? = null,
    val notice: RouteNotice? = null,
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
    val reason: RouteReason,
    val focusedDoorOutcome: FocusedDoorOutcome? = null,
) {
    val improvedDoorCount: Int
        get() {
            val improved = steps.flatMap(RouteStepUiState::doorsImproved).toSet()
            val opened = steps.flatMap(RouteStepUiState::doorsOpened).toSet()
            val lost = steps.flatMap(RouteStepUiState::doorsLost).toSet()
            return (improved - opened - lost).size
        }
    val lostDoorCount: Int get() = steps.flatMap(RouteStepUiState::doorsLost).distinct().size
}

enum class RouteNotice { SAVED, SAVE_FAILED }

enum class FocusedDoorOutcome { ASSESSED_CHECKS_PASS, CHECKS_IMPROVE }

enum class RouteReasonKind { OPENS_SAVED, OPENS, IMPROVES }

data class RouteReason(val kind: RouteReasonKind, val count: Int)

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
    val targetFactKey: FactKey? = null,
)

sealed interface RouteUiEvent {
    data class HorizonChanged(val days: Int) : RouteUiEvent

    data class MaxCostChanged(val cost: CostBand) : RouteUiEvent

    data class MaxEffortChanged(val effort: EffortLevel) : RouteUiEvent

    data class RouteSelected(val id: String) : RouteUiEvent

    data object SaveRoute : RouteUiEvent

    data object NoticeDismissed : RouteUiEvent
}
