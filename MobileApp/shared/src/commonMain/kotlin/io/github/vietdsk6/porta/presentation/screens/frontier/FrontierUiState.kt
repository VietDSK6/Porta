package io.github.vietdsk6.porta.presentation.screens.frontier

data class FrontierUiState(
    val isSampleMode: Boolean = true,
    val isSampleCatalog: Boolean = true,
    val catalogDoorCount: Int = 0,
    val horizonDays: Int = 90,
    val activePlan: ActivePlanSummaryUiState? = null,
    val actions: List<FrontierActionUiState> = emptyList(),
)

data class ActivePlanSummaryUiState(
    val title: String,
    val statusLabel: String,
)

data class FrontierActionUiState(
    val id: String,
    val title: String,
    val targetLabel: String,
    val newlyOpenCount: Int,
    val improvedCount: Int = newlyOpenCount,
    val savedDoorCount: Int,
    val timingUnverifiedCount: Int = 0,
    val durationLabel: String,
    val effortLabel: String,
    val costLabel: String,
    val explanation: String,
    val doorTitles: List<String>,
)

sealed interface FrontierUiEvent {
    data class HorizonChanged(val days: Int) : FrontierUiEvent
}
