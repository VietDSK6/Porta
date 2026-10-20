package io.github.vietdsk6.porta.presentation.screens.frontier

data class FrontierUiState(
    val isSampleMode: Boolean = true,
    val horizonDays: Int = 90,
    val actions: List<FrontierActionUiState> = emptyList(),
)

data class FrontierActionUiState(
    val id: String,
    val title: String,
    val targetLabel: String,
    val newlyOpenCount: Int,
    val savedDoorCount: Int,
    val durationLabel: String,
    val effortLabel: String,
    val costLabel: String,
    val explanation: String,
    val doorTitles: List<String>,
)

sealed interface FrontierUiEvent {
    data class HorizonChanged(val days: Int) : FrontierUiEvent
}
