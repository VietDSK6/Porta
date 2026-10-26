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
    val suggestedGrowthHorizonDays: Int? = null,
    val selectedActionId: String? = null,
) {
    val actionFamilies: List<FrontierActionFamilyUiState>
        get() = actions
            .groupBy(FrontierActionUiState::familyId)
            .map { (familyId, familyActions) ->
                FrontierActionFamilyUiState(
                    id = familyId,
                    title = familyActions.first().familyLabel,
                    kind = familyActions.first().kind,
                    actions = familyActions,
                )
            }

    val hasMeaningfulTradeOffs: Boolean
        get() = actions
            .map { action ->
                FrontierTradeOffVector(
                    durationDays = action.durationDays,
                    improvedCount = action.improvedCount,
                    cost = action.cost,
                )
            }
            .distinct()
            .size > 1
}

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
    val kind: FrontierMoveKind = FrontierMoveKind.IMPROVE,
    val familyId: String = id,
    val familyLabel: String = title,
    val scopeLabel: String = "Profile-wide",
    val estimateSourceLabel: String = "Time estimate · basis not stated",
    val impactBasisLabel: String = "Impact based on encoded rule simulation",
)

enum class FrontierMoveKind {
    VERIFY,
    IMPROVE,
    PREPARE,
}

data class FrontierActionFamilyUiState(
    val id: String,
    val title: String,
    val kind: FrontierMoveKind,
    val actions: List<FrontierActionUiState>,
)

private data class FrontierTradeOffVector(
    val durationDays: Int,
    val improvedCount: Int,
    val cost: CostBand,
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
