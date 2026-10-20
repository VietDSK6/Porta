package io.github.vietdsk6.porta.presentation.screens.activeplan

data class ActivePlanUiState(
    val isLoading: Boolean = true,
    val isWorking: Boolean = false,
    val plan: ActivePlanCardUiState? = null,
    val error: String? = null,
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
    val affectedDoorTitles: List<String>,
    val riskNote: String,
    val updatedLabel: String,
    val progress: Float,
    val checklist: List<PlanStepUiState>,
)

data class PlanStepUiState(
    val title: String,
    val isComplete: Boolean,
)

enum class ActivePlanStatusUi {
    PLANNED,
    IN_PROGRESS,
    PAUSED,
    COMPLETED,
}

sealed interface ActivePlanUiEvent {
    data object Start : ActivePlanUiEvent
    data object Pause : ActivePlanUiEvent
    data object Resume : ActivePlanUiEvent
    data object Complete : ActivePlanUiEvent
    data object Remove : ActivePlanUiEvent
}
