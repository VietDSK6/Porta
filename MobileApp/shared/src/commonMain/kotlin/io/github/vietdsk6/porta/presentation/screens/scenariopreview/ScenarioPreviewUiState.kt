package io.github.vietdsk6.porta.presentation.screens.scenariopreview

data class ScenarioPreviewUiState(
    val actionId: String = "",
    val title: String = "",
    val beforeLabel: String = "",
    val afterLabel: String = "",
    val scopeNotice: String? = null,
    val baselineCounts: ScenarioConclusionCountsUiState = ScenarioConclusionCountsUiState(),
    val simulatedCounts: ScenarioConclusionCountsUiState = ScenarioConclusionCountsUiState(),
    val affectedDoors: List<ScenarioDoorUiState> = emptyList(),
    val unchangedDoors: List<ScenarioDoorUiState> = emptyList(),
    val isSampleProfile: Boolean = true,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val saveError: String? = null,
    val error: String? = null,
) {
    val openedDoors: List<ScenarioDoorUiState>
        get() = affectedDoors.filter { it.outcome == ScenarioDoorOutcome.CURRENT_CHECKS_PASSED }

    val progressedDoors: List<ScenarioDoorUiState>
        get() = affectedDoors.filter { it.outcome != ScenarioDoorOutcome.CURRENT_CHECKS_PASSED }

    val hasCatalogCounts: Boolean
        get() = baselineCounts.total > 0
}

data class ScenarioConclusionCountsUiState(
    val currentChecksPassed: Int = 0,
    val needsAction: Int = 0,
    val needsInformation: Int = 0,
    val notAvailable: Int = 0,
    val closed: Int = 0,
) {
    val total: Int
        get() = currentChecksPassed + needsAction + needsInformation + notAvailable + closed
}

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
    val sourceUrl: String = "",
    val sourceSection: String = "",
    val lastCheckedLabel: String = "",
)

data class ScenarioRuleChangeUiState(
    val id: String,
    val explanation: String,
    val beforeLabel: String,
    val afterLabel: String,
    val resultTransitionLabel: String,
    val sourceNote: String = "",
)

sealed interface ScenarioPreviewUiEvent {
    data object Acknowledged : ScenarioPreviewUiEvent
    data object SavePlan : ScenarioPreviewUiEvent
    data object SaveHandled : ScenarioPreviewUiEvent
}
