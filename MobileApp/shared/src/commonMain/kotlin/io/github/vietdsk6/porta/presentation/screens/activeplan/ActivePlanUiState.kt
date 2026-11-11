package io.github.vietdsk6.porta.presentation.screens.activeplan

data class ActivePlanUiState(
    val isLoading: Boolean = true,
    val isWorking: Boolean = false,
    val isSampleCatalog: Boolean = true,
    val plan: ActivePlanCardUiState? = null,
    val routeSteps: List<ActiveRouteStepUiState> = emptyList(),
    val selectedPlanId: String? = null,
    val error: String? = null,
)

data class ActiveRouteStepUiState(
    val planId: String,
    val sequence: Int,
    val title: String,
)

data class ActivePlanCardUiState(
    val id: String,
    val actionId: String,
    val title: String,
    val targetLabel: String,
    val status: ActivePlanStatusUi,
    val statusLabel: String,
    val durationLabel: String,
    val effortLabel: String,
    val costLabel: String,
    val evidenceLabel: String,
    val candidateCheckChanges: List<PlanOpportunityImpactUiState>,
    val riskNote: String,
    val updatedLabel: String,
    val steps: List<PlanStepUiState>,
) {
    val candidateOpportunityIds: List<String>
        get() = candidateCheckChanges.map(PlanOpportunityImpactUiState::opportunityId)

    val notRuledOutOpportunityIds: List<String>
        get() = candidateCheckChanges.filter { it.timing.isNotRuledOut }.map(PlanOpportunityImpactUiState::opportunityId)

    val knownDeadlineFeasibleOpportunityIds: List<String>
        get() = candidateCheckChanges.filter { it.timing == PlanTimingStatus.WITHIN_KNOWN_DEADLINE }
            .map(PlanOpportunityImpactUiState::opportunityId)

    val deadlineMisses: List<PlanOpportunityImpactUiState>
        get() = candidateCheckChanges.filter { it.timing == PlanTimingStatus.MISSES_KNOWN_DEADLINE }

    val timingUnverified: List<PlanOpportunityImpactUiState>
        get() = candidateCheckChanges.filter { it.timing == PlanTimingStatus.TIMING_UNVERIFIED }
}

data class PlanOpportunityImpactUiState(
    val opportunityId: String,
    val title: String,
    val timing: PlanTimingStatus,
)

enum class PlanTimingStatus(val isNotRuledOut: Boolean) {
    WITHIN_KNOWN_DEADLINE(true),
    TIMING_UNVERIFIED(true),
    MISSES_KNOWN_DEADLINE(false),
    OUTSIDE_CURRENT_CYCLE(false),
}

data class PlanStepUiState(
    val title: String,
    val state: PlanStepState,
)

enum class PlanStepState {
    DONE,
    IN_PROGRESS,
    PAUSED,
    NEXT,
    LATER,
}

enum class ActivePlanStatusUi {
    PLANNED,
    IN_PROGRESS,
    PAUSED,
    COMPLETED,
}

sealed interface ActivePlanUiEvent {
    data class SelectStep(val planId: String) : ActivePlanUiEvent
    data object Start : ActivePlanUiEvent
    data object Pause : ActivePlanUiEvent
    data object Resume : ActivePlanUiEvent
    data object Complete : ActivePlanUiEvent
    data object Remove : ActivePlanUiEvent
}
