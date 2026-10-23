package io.github.vietdsk6.porta.presentation.screens.frontier

import io.github.vietdsk6.porta.domain.model.CostBand
import io.github.vietdsk6.porta.domain.model.EffortLevel

data class FrontierUiState(
    val isSampleMode: Boolean = true,
    val isSampleCatalog: Boolean = true,
    val catalogDoorCount: Int = 0,
    val horizonDays: Int = 90,
    val maxEffort: EffortLevel = EffortLevel.VERY_HIGH,
    val maxCost: CostBand = CostBand.UNKNOWN,
    val prioritizeSaved: Boolean = true,
    val activePlan: ActivePlanSummaryUiState? = null,
    val actions: List<FrontierActionUiState> = emptyList(),
    val exclusions: List<FrontierExcludedActionUiState> = emptyList(),
    val selectedActionId: String? = null,
)

data class ActivePlanSummaryUiState(
    val title: String,
    val statusLabel: String,
)

data class FrontierActionUiState(
    val id: String,
    val title: String,
    val currentLabel: String = "Not set",
    val targetLabel: String,
    val newlyOpenCount: Int,
    val improvedCount: Int = newlyOpenCount,
    val savedDoorCount: Int,
    val timingUnverifiedCount: Int = 0,
    val durationDays: Int,
    val effort: EffortLevel,
    val cost: CostBand,
    val durationLabel: String,
    val effortLabel: String,
    val costLabel: String,
    val explanation: String,
    val doorTitles: List<String>,
    val doorComparisons: List<FrontierDoorComparisonUiState> = emptyList(),
)

data class FrontierDoorComparisonUiState(
    val id: String,
    val title: String,
    val actualStateLabel: String,
    val previewStateLabel: String,
    val isNewlyOpen: Boolean,
    val isSaved: Boolean,
    val requiresDeadlineCheck: Boolean,
)

data class FrontierExcludedActionUiState(
    val id: String,
    val title: String,
    val reasons: List<String>,
)

sealed interface FrontierUiEvent {
    data class HorizonChanged(val days: Int) : FrontierUiEvent

    data class MaxEffortChanged(val effort: EffortLevel) : FrontierUiEvent

    data class MaxCostChanged(val cost: CostBand) : FrontierUiEvent

    data class PrioritizeSavedChanged(val prioritize: Boolean) : FrontierUiEvent

    data class ActionSelected(val actionId: String) : FrontierUiEvent
}
