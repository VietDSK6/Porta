package io.github.vietdsk6.porta.presentation.screens.frontier

import io.github.vietdsk6.porta.domain.model.CostBand
import io.github.vietdsk6.porta.domain.model.EffortLevel
import io.github.vietdsk6.porta.domain.model.EligibilityState
import io.github.vietdsk6.porta.domain.model.ExplanationCode
import io.github.vietdsk6.porta.domain.model.FactKey
import io.github.vietdsk6.porta.domain.model.FactValue
import io.github.vietdsk6.porta.domain.model.FrontierExclusionReason
import io.github.vietdsk6.porta.domain.model.PlanStatus
import kotlinx.datetime.LocalDate

data class FrontierUiState(
    val isSampleMode: Boolean = true,
    val isDemoProfile: Boolean = false,
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
    val comparedActionIds: List<String> = emptyList(),
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

    val comparedActions: List<FrontierActionUiState>
        get() = comparedActionIds.mapNotNull { comparedId ->
            actions.firstOrNull { action -> action.id == comparedId }
        }
}

data class ActivePlanSummaryUiState(
    val title: String,
    val status: PlanStatus,
    val statusLabel: String,
) {
    val needsPriorityPlacement: Boolean
        get() = status != PlanStatus.PLANNED
}

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
    val explanationCode: ExplanationCode.FrontierImpact? = null,
    val doorTitles: List<String>,
    val doorComparisons: List<FrontierDoorComparisonUiState> = emptyList(),
    val kind: FrontierMoveKind = FrontierMoveKind.IMPROVE,
    val familyId: String = id,
    val familyLabel: String = title,
    val scopeLabel: String = "Profile-wide",
    val estimateSourceLabel: String = "Time estimate · basis not stated",
    val impactBasisLabel: String = "Impact based on encoded rule simulation",
    val earliestDeadlineLabel: String? = null,
    val changedCheckCount: Int = improvedCount,
    val programmeTitle: String? = null,
    val targetFactKey: FactKey? = null,
    val currentFallback: FrontierCurrentFallback? = null,
    val scope: FrontierScope? = null,
    val estimateBasis: FrontierEstimateBasis? = null,
    val impactDeadlineUnconfirmed: Boolean = false,
    val earliestAffectedDeadline: LocalDate? = null,
    val currentValue: FactValue? = null,
    val targetValue: FactValue? = null,
    val titleCode: FrontierTitleCode? = null,
)

enum class FrontierCurrentFallback { NOT_SET, NOT_CONFIRMED }

enum class FrontierScope { PROFILE, PROGRAMME }

enum class FrontierEstimateBasis { SYNTHETIC, PORTA_ASSUMPTION, UNSTATED }

enum class FrontierTitleCode { VERIFY_AVAILABILITY, VERIFY_ENROLMENT, VERIFY_REQUIREMENT, COMPLETE_DEGREE, ACTIVE_ENROLMENT, RELEVANT_EXPERIENCE, RAISE_IELTS }

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
    val needsVerification: Boolean = false,
    val isSaved: Boolean,
    val requiresDeadlineCheck: Boolean,
    val actualState: EligibilityState? = null,
    val previewState: EligibilityState? = null,
)

data class FrontierExcludedActionUiState(
    val id: String,
    val title: String,
    val reasons: List<String>,
    val reasonCodes: List<FrontierExclusionReason> = emptyList(),
    val titleCode: FrontierTitleCode? = null,
    val targetValue: FactValue? = null,
    val programmeTitle: String? = null,
)

sealed interface FrontierUiEvent {
    data class HorizonChanged(val days: Int) : FrontierUiEvent

    data class MaxEffortChanged(val effort: EffortLevel) : FrontierUiEvent

    data class MaxCostChanged(val cost: CostBand) : FrontierUiEvent

    data class PrioritizeSavedChanged(val prioritize: Boolean) : FrontierUiEvent

    data class ActionSelected(val actionId: String) : FrontierUiEvent

    data class ComparisonToggled(val actionId: String) : FrontierUiEvent

    data object ComparisonCleared : FrontierUiEvent
}

internal const val MAX_COMPARED_ACTIONS = 3
