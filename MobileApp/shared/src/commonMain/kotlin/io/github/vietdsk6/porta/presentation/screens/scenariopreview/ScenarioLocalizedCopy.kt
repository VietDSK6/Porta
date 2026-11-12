package io.github.vietdsk6.porta.presentation.screens.scenariopreview

import androidx.compose.runtime.Composable
import io.github.vietdsk6.porta.domain.model.Deadline
import io.github.vietdsk6.porta.domain.model.EligibilityState
import io.github.vietdsk6.porta.domain.model.RuleResult
import io.github.vietdsk6.porta.generated.resources.*
import io.github.vietdsk6.porta.presentation.locale.formatLocalDate
import io.github.vietdsk6.porta.presentation.locale.toText
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ScenarioPreviewUiState.localizedError(): String = when (errorCode) {
    ScenarioErrorCode.ACTION_MISSING -> stringResource(Res.string.scenario_error_action_missing)
    ScenarioErrorCode.FACT_MISSING -> stringResource(Res.string.scenario_error_fact_missing, errorFactKey?.toText().orEmpty())
    else -> error.orEmpty()
}

@Composable
internal fun ScenarioPreviewUiState.localizedSaveError(): String = when (saveErrorCode) {
    ScenarioErrorCode.SAVE_NEEDS_PROFILE -> stringResource(Res.string.scenario_error_need_profile)
    ScenarioErrorCode.SAVE_FAILED -> stringResource(Res.string.scenario_error_save_failed)
    else -> saveError.orEmpty()
}

@Composable
internal fun ScenarioPreviewUiState.localizedScopeNotice(): String = if (hasRuleScopeNotice) {
    stringResource(Res.string.scenario_rule_scope)
} else {
    scopeNotice.orEmpty()
}

@Composable
internal fun ScenarioPreviewUiState.localizedBefore(): String = if (beforeNotConfirmed) {
    stringResource(Res.string.scenario_not_confirmed)
} else {
    beforeLabel
}

@Composable
internal fun ScenarioDoorUiState.localizedOutcome(): String = when (outcomeCode) {
    ScenarioOutcomeCode.MISSED_DEADLINE -> stringResource(Res.string.scenario_outcome_deadline)
    ScenarioOutcomeCode.PASSED -> stringResource(Res.string.scenario_outcome_passed)
    ScenarioOutcomeCode.VERIFICATION -> stringResource(Res.string.scenario_outcome_verification)
    ScenarioOutcomeCode.ONE_CHECK -> stringResource(Res.string.scenario_outcome_one_check)
    ScenarioOutcomeCode.NO_CHECK_CHANGE -> stringResource(Res.string.scenario_outcome_no_change)
    null -> stateLabel
}

@Composable
internal fun ScenarioDoorUiState.localizedActualState(): String = actualState?.localizedState() ?: actualStateLabel

@Composable
internal fun ScenarioDoorUiState.localizedSimulatedState(): String = simulatedState?.localizedState() ?: simulatedStateLabel

@Composable
internal fun ScenarioDoorUiState.localizedTiming(): String = when (val value = deadline) {
    is Deadline.Known -> stringResource(Res.string.scenario_deadline_known, formatLocalDate(value.date))
    Deadline.Tba -> stringResource(Res.string.scenario_deadline_tba)
    Deadline.Missing -> stringResource(Res.string.scenario_deadline_missing)
    null -> timingLabel
}

@Composable
internal fun ScenarioDoorUiState.localizedTimingWarning(): String = timingWarningDays?.let {
    stringResource(Res.string.scenario_estimate_exceeds, it)
} ?: timingWarning.orEmpty()

@Composable
internal fun ScenarioDoorUiState.localizedChecked(): String = lastCheckedDate?.let {
    stringResource(Res.string.scenario_checked_date, formatLocalDate(it))
} ?: lastCheckedLabel

@Composable
internal fun ScenarioRuleChangeUiState.localizedResultTransition(): String = if (beforeResult != null && afterResult != null) {
    stringResource(Res.string.scenario_state_transition, beforeResult.localizedResult(), afterResult.localizedResult())
} else {
    resultTransitionLabel
}

@Composable
internal fun ScenarioRuleChangeUiState.localizedBefore(): String = if (beforeMissing) {
    stringResource(Res.string.scenario_not_provided)
} else {
    beforeLabel
}

@Composable
internal fun ScenarioRuleChangeUiState.localizedAfter(): String = if (afterMissing) {
    stringResource(Res.string.scenario_not_provided)
} else {
    afterLabel
}

@Composable
internal fun ScenarioRemainingBlockerUiState.localizedText(): String = when (result) {
    RuleResult.FAIL -> stringResource(Res.string.scenario_block_action, explanation)
    RuleResult.UNKNOWN -> stringResource(Res.string.scenario_block_information, explanation)
    RuleResult.REVIEW -> stringResource(Res.string.scenario_block_review, explanation)
    RuleResult.PASS -> explanation
}

@Composable
private fun EligibilityState.localizedState(): String = stringResource(
    when (this) {
        EligibilityState.OPEN -> Res.string.eligibility_status_passed
        EligibilityState.HAS_GAPS -> Res.string.eligibility_status_needs_action
        EligibilityState.NEEDS_INFO -> Res.string.eligibility_status_needs_info
        EligibilityState.NOT_ACTIONABLE -> Res.string.eligibility_status_not_available
        EligibilityState.CLOSED -> Res.string.eligibility_status_closed
    },
)

@Composable
private fun RuleResult.localizedResult(): String = stringResource(
    when (this) {
        RuleResult.PASS -> Res.string.scenario_result_passed
        RuleResult.FAIL -> Res.string.scenario_result_action
        RuleResult.UNKNOWN -> Res.string.scenario_result_missing
        RuleResult.REVIEW -> Res.string.scenario_result_review
    },
)
