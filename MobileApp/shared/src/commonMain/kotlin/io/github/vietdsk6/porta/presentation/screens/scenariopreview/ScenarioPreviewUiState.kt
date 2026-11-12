package io.github.vietdsk6.porta.presentation.screens.scenariopreview

import io.github.vietdsk6.porta.domain.model.Deadline
import io.github.vietdsk6.porta.domain.model.EligibilityState
import io.github.vietdsk6.porta.domain.model.FactKey
import io.github.vietdsk6.porta.domain.model.RuleResult
import kotlinx.datetime.LocalDate

data class ScenarioPreviewUiState(
    val actionId: String = "",
    val title: String = "",
    val beforeLabel: String = "",
    val afterLabel: String = "",
    val scopeNotice: String? = null,
    val hasRuleScopeNotice: Boolean = false,
    val beforeNotConfirmed: Boolean = false,
    val baselineCounts: ScenarioConclusionCountsUiState = ScenarioConclusionCountsUiState(),
    val simulatedCounts: ScenarioConclusionCountsUiState = ScenarioConclusionCountsUiState(),
    val affectedDoors: List<ScenarioDoorUiState> = emptyList(),
    val unchangedDoors: List<ScenarioDoorUiState> = emptyList(),
    val isSampleProfile: Boolean = true,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val saveError: String? = null,
    val saveErrorCode: ScenarioErrorCode? = null,
    val error: String? = null,
    val errorCode: ScenarioErrorCode? = null,
    val errorFactKey: FactKey? = null,
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

enum class ScenarioErrorCode { ACTION_MISSING, FACT_MISSING, SAVE_NEEDS_PROFILE, SAVE_FAILED }

enum class ScenarioOutcomeCode { MISSED_DEADLINE, PASSED, VERIFICATION, ONE_CHECK, NO_CHECK_CHANGE }

data class ScenarioDoorUiState(
    val id: String,
    val title: String,
    val stateLabel: String,
    val outcome: ScenarioDoorOutcome,
    val outcomeCode: ScenarioOutcomeCode? = null,
    val actualState: EligibilityState? = null,
    val simulatedState: EligibilityState? = null,
    val actualStateLabel: String = "",
    val simulatedStateLabel: String = "",
    val changedRules: List<ScenarioRuleChangeUiState> = emptyList(),
    val remainingBlockers: List<String> = emptyList(),
    val remainingBlockerDetails: List<ScenarioRemainingBlockerUiState> = emptyList(),
    val timingLabel: String = "",
    val deadline: Deadline? = null,
    val timingWarning: String? = null,
    val timingWarningDays: Int? = null,
    val sourceUrl: String = "",
    val sourceSection: String = "",
    val lastCheckedLabel: String = "",
    val lastCheckedDate: LocalDate? = null,
)

data class ScenarioRemainingBlockerUiState(val result: RuleResult, val explanation: String)

data class ScenarioRuleChangeUiState(
    val id: String,
    val explanation: String,
    val beforeLabel: String,
    val afterLabel: String,
    val resultTransitionLabel: String,
    val beforeResult: RuleResult? = null,
    val afterResult: RuleResult? = null,
    val beforeMissing: Boolean = false,
    val afterMissing: Boolean = false,
    val sourceNote: String = "",
)

sealed interface ScenarioPreviewUiEvent {
    data object Acknowledged : ScenarioPreviewUiEvent
    data object SavePlan : ScenarioPreviewUiEvent
    data object SaveHandled : ScenarioPreviewUiEvent
}
