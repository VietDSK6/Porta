package io.github.vietdsk6.porta.presentation.screens.activeplan

import androidx.compose.runtime.Composable
import io.github.vietdsk6.porta.generated.resources.*
import io.github.vietdsk6.porta.presentation.locale.asLocalizedDate
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ActivePlanCardUiState.localizedStatus(): String = stringResource(
    when (status) {
        ActivePlanStatusUi.PLANNED -> Res.string.active_copy_planned
        ActivePlanStatusUi.IN_PROGRESS -> Res.string.active_copy_in_progress
        ActivePlanStatusUi.PAUSED -> Res.string.active_copy_paused
        ActivePlanStatusUi.COMPLETED -> Res.string.active_copy_completed
    },
)

@Composable
internal fun ActivePlanCardUiState.localizedDuration(): String = when (durationKind) {
    PlanDurationKind.ESTIMATE -> if (durationDays == null) durationLabel else stringResource(Res.string.active_copy_estimate, durationDays)
    PlanDurationKind.MINIMUM -> if (durationDays == null) durationLabel else stringResource(Res.string.active_copy_minimum, durationDays)
    PlanDurationKind.UNKNOWN -> stringResource(Res.string.active_copy_unknown_duration)
    null -> durationLabel
}

@Composable
internal fun ActivePlanCardUiState.localizedEffort(): String {
    val name = effortName ?: return effortLabel
    val level = when (name) {
        "VERY_LOW" -> stringResource(Res.string.active_copy_effort_very_low)
        "LOW" -> stringResource(Res.string.active_copy_effort_low)
        "MEDIUM" -> stringResource(Res.string.active_copy_effort_medium)
        "HIGH" -> stringResource(Res.string.active_copy_effort_high)
        "VERY_HIGH" -> stringResource(Res.string.active_copy_effort_very_high)
        else -> name
    }
    return stringResource(Res.string.active_copy_effort, level)
}

@Composable
internal fun ActivePlanCardUiState.localizedCost(): String {
    val name = costName ?: return costLabel
    val level = when (name) {
        "FREE" -> stringResource(Res.string.active_copy_cost_free)
        "LOW" -> stringResource(Res.string.active_copy_cost_low)
        "MEDIUM" -> stringResource(Res.string.active_copy_cost_medium)
        "HIGH" -> stringResource(Res.string.active_copy_cost_high)
        "UNKNOWN" -> stringResource(Res.string.active_copy_cost_unknown)
        else -> name
    }
    return stringResource(Res.string.active_copy_cost, level)
}

@Composable
internal fun ActivePlanCardUiState.localizedEvidence(): String = if (evidenceDefinitionMissing) {
    stringResource(Res.string.active_copy_evidence_review)
} else {
    evidenceLabel
}

@Composable
internal fun ActivePlanCardUiState.localizedUpdated(): String {
    val timestamp = updatedAtEpochMillis ?: return updatedLabel
    return stringResource(Res.string.active_copy_updated, timestamp.asLocalizedDate())
}

@Composable
internal fun ActivePlanCardUiState.localizedRisk(): String = when {
    isRouteProjection -> stringResource(Res.string.active_route_projection_risk)
    durationKind != null -> stringResource(Res.string.active_copy_risk)
    else -> riskNote
}

@Composable
internal fun PlanStepUiState.localizedTitle(): String {
    val key = copy ?: return title
    return stringResource(
        when (key) {
            PlanStepCopy.SAVED -> Res.string.active_copy_step_saved
            PlanStepCopy.START -> Res.string.active_copy_step_start
            PlanStepCopy.IN_PROGRESS -> Res.string.active_copy_step_in_progress
            PlanStepCopy.PAUSED -> Res.string.active_copy_step_paused
            PlanStepCopy.COMPLETE -> Res.string.active_copy_step_complete
            PlanStepCopy.CONFIRM_RESULT -> Res.string.active_copy_step_confirm
        },
    )
}

@Composable
internal fun ActivePlanUiState.localizedError(): String? = if (errorCode != null) {
    stringResource(Res.string.active_copy_update_failed)
} else {
    error
}
