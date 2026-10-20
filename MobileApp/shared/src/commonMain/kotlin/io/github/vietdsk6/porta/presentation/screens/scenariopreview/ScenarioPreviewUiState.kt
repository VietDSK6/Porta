package io.github.vietdsk6.porta.presentation.screens.scenariopreview

data class ScenarioPreviewUiState(
    val actionId: String = "",
    val title: String = "",
    val beforeLabel: String = "",
    val afterLabel: String = "",
    val openedDoors: List<ScenarioDoorUiState> = emptyList(),
    val unchangedDoors: List<ScenarioDoorUiState> = emptyList(),
    val isSampleProfile: Boolean = true,
    val error: String? = null,
)

data class ScenarioDoorUiState(
    val id: String,
    val title: String,
    val stateLabel: String,
)

sealed interface ScenarioPreviewUiEvent {
    data object Acknowledged : ScenarioPreviewUiEvent
}
