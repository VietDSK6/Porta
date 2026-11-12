package io.github.vietdsk6.porta.presentation.screens.frontier

import androidx.compose.runtime.Composable
import io.github.vietdsk6.porta.domain.model.CostBand
import io.github.vietdsk6.porta.domain.model.EffortLevel
import io.github.vietdsk6.porta.domain.model.FactKey
import io.github.vietdsk6.porta.domain.model.FactValue
import io.github.vietdsk6.porta.domain.model.FrontierExclusionReason
import io.github.vietdsk6.porta.generated.resources.*
import io.github.vietdsk6.porta.presentation.components.trustLabelText
import io.github.vietdsk6.porta.presentation.locale.displayValue
import io.github.vietdsk6.porta.presentation.locale.formatLocalDate
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun FrontierDoorComparisonUiState.actualText(): String = actualState?.trustLabelText() ?: actualStateLabel

@Composable
internal fun FrontierDoorComparisonUiState.previewText(): String = previewState?.trustLabelText() ?: previewStateLabel

@Composable
internal fun FrontierActionUiState.currentText(): String = currentValue?.displayValue() ?: when (currentFallback) {
    FrontierCurrentFallback.NOT_SET -> stringResource(Res.string.frontier_current_not_set)
    FrontierCurrentFallback.NOT_CONFIRMED -> stringResource(Res.string.frontier_current_not_confirmed)
    null -> currentLabel
}

@Composable
internal fun FrontierActionUiState.targetText(): String = targetValue?.displayValue() ?: targetLabel

@Composable
internal fun FrontierActionUiState.titleText(): String = frontierTitleText(titleCode, title, programmeTitle, targetValue)

@Composable
internal fun FrontierExcludedActionUiState.titleText(): String = frontierTitleText(titleCode, title, programmeTitle, targetValue)

@Composable
private fun frontierTitleText(code: FrontierTitleCode?, title: String, programmeTitle: String?, targetValue: FactValue?): String = when (code) {
    FrontierTitleCode.VERIFY_AVAILABILITY -> stringResource(Res.string.frontier_title_verify_availability, programmeTitle.orEmpty())

    FrontierTitleCode.VERIFY_ENROLMENT -> stringResource(Res.string.frontier_title_verify_enrolment, programmeTitle.orEmpty())

    FrontierTitleCode.VERIFY_REQUIREMENT -> stringResource(Res.string.frontier_title_verify_requirement, programmeTitle.orEmpty())

    FrontierTitleCode.COMPLETE_DEGREE -> stringResource(Res.string.frontier_title_complete_degree)

    FrontierTitleCode.ACTIVE_ENROLMENT -> stringResource(Res.string.frontier_title_active_enrolment)

    FrontierTitleCode.RELEVANT_EXPERIENCE -> {
        val months = (targetValue as? FactValue.NumberValue)?.amount?.toInt()
        if (months == null) title else stringResource(Res.string.frontier_title_experience, months)
    }

    FrontierTitleCode.RAISE_IELTS -> {
        val band = (targetValue as? FactValue.NumberValue)?.amount
        if (band == null) title else stringResource(Res.string.frontier_title_ielts, band.toString())
    }

    null -> title
}

@Composable
internal fun FrontierActionUiState.durationText(): String = when (estimateBasis) {
    FrontierEstimateBasis.UNSTATED -> stringResource(Res.string.frontier_duration_assumed, durationDays)

    FrontierEstimateBasis.SYNTHETIC, FrontierEstimateBasis.PORTA_ASSUMPTION ->
        stringResource(Res.string.frontier_duration_estimated, durationDays)

    null -> durationLabel
}

@Composable
internal fun FrontierActionUiState.scopeText(): String = when (scope) {
    FrontierScope.PROFILE -> stringResource(Res.string.frontier_scope_profile)
    FrontierScope.PROGRAMME -> stringResource(Res.string.frontier_scope_programme)
    null -> scopeLabel
}

@Composable
internal fun FrontierActionUiState.estimateSourceText(): String = when (estimateBasis) {
    FrontierEstimateBasis.SYNTHETIC -> stringResource(Res.string.frontier_estimate_synthetic)
    FrontierEstimateBasis.PORTA_ASSUMPTION -> stringResource(Res.string.frontier_estimate_assumption)
    FrontierEstimateBasis.UNSTATED -> stringResource(Res.string.frontier_estimate_unstated)
    null -> estimateSourceLabel
}

@Composable
internal fun FrontierActionUiState.impactBasisText(): String = if (estimateBasis == null) {
    impactBasisLabel
} else if (impactDeadlineUnconfirmed) {
    stringResource(Res.string.frontier_impact_basis_unconfirmed)
} else {
    stringResource(Res.string.frontier_impact_basis)
}

@Composable
internal fun FrontierActionUiState.earliestDeadlineText(): String? = earliestAffectedDeadline?.let {
    stringResource(Res.string.frontier_earliest_deadline, formatLocalDate(it))
} ?: earliestDeadlineLabel

@Composable
internal fun FrontierActionUiState.effortText(): String = if (estimateBasis == null) effortLabel else effort.frontierEffortText()

@Composable
internal fun FrontierActionUiState.costText(): String = if (estimateBasis == null) costLabel else cost.frontierCostText()

@Composable
internal fun EffortLevel.frontierEffortText(): String = stringResource(
    when (this) {
        EffortLevel.VERY_LOW -> Res.string.frontier_effort_very_low
        EffortLevel.LOW -> Res.string.frontier_effort_low
        EffortLevel.MEDIUM -> Res.string.frontier_effort_medium
        EffortLevel.HIGH -> Res.string.frontier_effort_high
        EffortLevel.VERY_HIGH -> Res.string.frontier_effort_very_high
    },
)

@Composable
internal fun CostBand.frontierCostText(): String = stringResource(
    when (this) {
        CostBand.FREE -> Res.string.frontier_cost_free
        CostBand.LOW -> Res.string.frontier_cost_low
        CostBand.MEDIUM -> Res.string.frontier_cost_medium
        CostBand.HIGH -> Res.string.frontier_cost_high
        CostBand.UNKNOWN -> Res.string.frontier_cost_unknown
    },
)

@Composable
internal fun FrontierActionFamilyUiState.familyText(): String = when (actions.firstOrNull()?.targetFactKey) {
    FactKey.AVAILABLE_FULL_PERIOD -> stringResource(Res.string.frontier_family_availability)

    FactKey.IELTS_SCORE -> stringResource(Res.string.frontier_family_ielts)

    FactKey.DEGREE_COMPLETED -> stringResource(Res.string.frontier_family_degree)

    FactKey.ENROLLED -> stringResource(Res.string.frontier_family_enrolment)

    FactKey.EXPERIENCE_MONTHS -> stringResource(Res.string.frontier_family_experience)

    else -> if (kind == FrontierMoveKind.VERIFY && actions.firstOrNull()?.targetFactKey != null) {
        stringResource(Res.string.frontier_family_requirement)
    } else {
        title
    }
}

@Composable
internal fun FrontierExclusionReason.reasonText(): String = stringResource(
    when (this) {
        FrontierExclusionReason.BEYOND_HORIZON -> Res.string.frontier_exclusion_horizon
        FrontierExclusionReason.ABOVE_MAX_EFFORT -> Res.string.frontier_exclusion_effort
        FrontierExclusionReason.ABOVE_MAX_COST -> Res.string.frontier_exclusion_cost
        FrontierExclusionReason.MISSES_KNOWN_DEADLINES -> Res.string.frontier_exclusion_deadline
        FrontierExclusionReason.NO_VERIFIED_IMPACT -> Res.string.frontier_exclusion_no_impact
        FrontierExclusionReason.DOMINATED -> Res.string.frontier_exclusion_dominated
    },
)
