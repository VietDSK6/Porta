package io.github.vietdsk6.porta.presentation.screens.scenariopreview

data class ScenarioPreviewUiState(
    val actionId: String = "",
    val title: String = "",
    val beforeLabel: String = "",
    val afterLabel: String = "",
    val scopeNotice: String? = null,
    val openedDoors: List<ScenarioDoorUiState> = emptyList(),
    val progressedDoors: List<ScenarioDoorUiState> = emptyList(),
    val unchangedDoors: List<ScenarioDoorUiState> = emptyList(),
    val isSampleProfile: Boolean = true,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val saveError: String? = null,
    val error: String? = null,
)

data class ScenarioDoorUiState(
    val id: String,
    val title: String,
    val stateLabel: String,
    val actualStateLabel: String = "",
    val simulatedStateLabel: String = "",
)

sealed interface ScenarioPreviewUiEvent {
    data object Acknowledged : ScenarioPreviewUiEvent
    data object SavePlan : ScenarioPreviewUiEvent
    data object SaveHandled : ScenarioPreviewUiEvent
}
