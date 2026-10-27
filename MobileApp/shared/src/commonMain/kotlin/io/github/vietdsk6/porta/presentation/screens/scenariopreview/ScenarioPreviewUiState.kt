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

enum class ScenarioDoorOutcome {
    CURRENT_CHECKS_PASSED,
    CHECKED_PROGRESS,
    NEEDS_VERIFICATION,
}

data class ScenarioDoorUiState(
    val id: String,
    val title: String,
    val stateLabel: String,
    val outcome: ScenarioDoorOutcome,
    val actualStateLabel: String = "",
    val simulatedStateLabel: String = "",
    val changedRules: List<ScenarioRuleChangeUiState> = emptyList(),
    val remainingBlockers: List<String> = emptyList(),
    val timingLabel: String = "",
    val timingWarning: String? = null,
)

data class ScenarioRuleChangeUiState(
    val id: String,
    val explanation: String,
    val beforeLabel: String,
    val afterLabel: String,
    val resultTransitionLabel: String,
)

sealed interface ScenarioPreviewUiEvent {
    data object Acknowledged : ScenarioPreviewUiEvent
    data object SavePlan : ScenarioPreviewUiEvent
    data object SaveHandled : ScenarioPreviewUiEvent
}
