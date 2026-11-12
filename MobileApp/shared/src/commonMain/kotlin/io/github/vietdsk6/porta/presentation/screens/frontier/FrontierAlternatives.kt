package io.github.vietdsk6.porta.presentation.screens.frontier

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.vietdsk6.porta.designsystem.theme.AppTheme
import io.github.vietdsk6.porta.domain.model.PlanStatus
import io.github.vietdsk6.porta.generated.resources.Res
import io.github.vietdsk6.porta.generated.resources.frontier_adjust
import io.github.vietdsk6.porta.generated.resources.frontier_adjust_limits
import io.github.vietdsk6.porta.generated.resources.frontier_collapsed
import io.github.vietdsk6.porta.generated.resources.frontier_compare
import io.github.vietdsk6.porta.generated.resources.frontier_compare_metrics
import io.github.vietdsk6.porta.generated.resources.frontier_deadline_confirmation
import io.github.vietdsk6.porta.generated.resources.frontier_exclusions_notice
import io.github.vietdsk6.porta.generated.resources.frontier_expanded
import io.github.vietdsk6.porta.generated.resources.frontier_family_shared_pattern
import io.github.vietdsk6.porta.generated.resources.frontier_hide
import io.github.vietdsk6.porta.generated.resources.frontier_hide_tradeoffs
import io.github.vietdsk6.porta.generated.resources.frontier_impact_assessed_checks
import io.github.vietdsk6.porta.generated.resources.frontier_impact_opportunities
import io.github.vietdsk6.porta.generated.resources.frontier_impact_summary
import io.github.vietdsk6.porta.generated.resources.frontier_improve_description
import io.github.vietdsk6.porta.generated.resources.frontier_kind_improve
import io.github.vietdsk6.porta.generated.resources.frontier_kind_prepare
import io.github.vietdsk6.porta.generated.resources.frontier_kind_verify
import io.github.vietdsk6.porta.generated.resources.frontier_no_compare
import io.github.vietdsk6.porta.generated.resources.frontier_no_feasible_help
import io.github.vietdsk6.porta.generated.resources.frontier_no_fit
import io.github.vietdsk6.porta.generated.resources.frontier_no_reliable_help
import io.github.vietdsk6.porta.generated.resources.frontier_no_reliable_move
import io.github.vietdsk6.porta.generated.resources.frontier_outside_limits
import io.github.vietdsk6.porta.generated.resources.frontier_plan_continue
import io.github.vietdsk6.porta.generated.resources.frontier_plan_current
import io.github.vietdsk6.porta.generated.resources.frontier_plan_open
import io.github.vietdsk6.porta.generated.resources.frontier_plan_open_to_confirm
import io.github.vietdsk6.porta.generated.resources.frontier_plan_result_to_confirm
import io.github.vietdsk6.porta.generated.resources.frontier_plan_review_paused
import io.github.vietdsk6.porta.generated.resources.frontier_plan_status_in_progress
import io.github.vietdsk6.porta.generated.resources.frontier_plan_status_paused
import io.github.vietdsk6.porta.generated.resources.frontier_plan_status_planned
import io.github.vietdsk6.porta.generated.resources.frontier_plot_a11y
import io.github.vietdsk6.porta.generated.resources.frontier_prepare_description
import io.github.vietdsk6.porta.generated.resources.frontier_preview_changes
import io.github.vietdsk6.porta.generated.resources.frontier_ripple_passed
import io.github.vietdsk6.porta.generated.resources.frontier_ripple_progress
import io.github.vietdsk6.porta.generated.resources.frontier_ripple_verification
import io.github.vietdsk6.porta.generated.resources.frontier_suggestion_improve
import io.github.vietdsk6.porta.generated.resources.frontier_suggestion_prepare
import io.github.vietdsk6.porta.generated.resources.frontier_suggestion_verify
import io.github.vietdsk6.porta.generated.resources.frontier_timing_caveat
import io.github.vietdsk6.porta.generated.resources.frontier_tradeoff_title
import io.github.vietdsk6.porta.generated.resources.frontier_tradeoffs_subtitle
import io.github.vietdsk6.porta.generated.resources.frontier_verify_description
import io.github.vietdsk6.porta.presentation.components.porta.ActionImpactNodeUiState
import io.github.vietdsk6.porta.presentation.components.porta.ImpactOutcome
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun AlternativeFamilySection(
    family: FrontierActionFamilyUiState,
    selectedActionId: String?,
    onSelect: (String) -> Unit,
    onPreview: (String) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth().testTag("frontier-family-${family.id}"),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text = "${family.kind.label()} · ${family.familyText()}",
                color = AppTheme.colors.primary,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = if (family.actions.size == 1) {
                    family.kind.description()
                } else {
                    stringResource(Res.string.frontier_family_shared_pattern, family.actions.size)
                },
                color = AppTheme.colors.text.secondary,
                style = AppTheme.typography.bodySmall,
            )
        }
        family.actions.forEachIndexed { index, action ->
            if (index > 0) {
                HorizontalDivider(color = AppTheme.colors.outline.copy(alpha = 0.25f))
            }
            AlternativeActionRow(
                action = action,
                isSelected = action.id == selectedActionId,
                isSelectable = selectedActionId != null,
                onSelect = { onSelect(action.id) },
                onPreview = { onPreview(action.id) },
            )
        }
    }
}

@Composable
internal fun AlternativeActionRow(
    action: FrontierActionUiState,
    isSelected: Boolean,
    isSelectable: Boolean,
    onSelect: () -> Unit,
    onPreview: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("frontier-action-${action.id}")
            .then(
                if (isSelectable) {
                    Modifier.selectable(
                        selected = isSelected,
                        role = Role.RadioButton,
                        onClick = onSelect,
                    )
                } else {
                    Modifier
                },
            ),
        color = if (isSelected) AppTheme.colors.alternative else Color.Transparent,
        shape = RoundedCornerShape(14.dp),
        border = if (isSelected) BorderStroke(1.dp, AppTheme.colors.primary) else null,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            Text(action.scopeText(), color = AppTheme.colors.text.secondary, style = AppTheme.typography.bodySmall)
            Text(action.programmeTitle ?: action.titleText(), style = AppTheme.typography.h6)
            Text(
                text = action.impactSummary(),
                color = AppTheme.colors.status.success,
                fontWeight = FontWeight.SemiBold,
                style = AppTheme.typography.bodyMedium,
            )
            if (action.kind == FrontierMoveKind.VERIFY) {
                Text(
                    text = stringResource(Res.string.frontier_timing_caveat),
                    color = AppTheme.colors.text.secondary,
                    style = AppTheme.typography.bodySmall,
                )
            } else {
                Text(
                    text = stringResource(Res.string.frontier_compare_metrics, action.durationText(), action.effortText(), action.costText()),
                    color = AppTheme.colors.text.secondary,
                    style = AppTheme.typography.bodySmall,
                )
            }
            TextButton(
                modifier = Modifier.align(Alignment.End),
                onClick = onPreview,
            ) {
                Text(stringResource(Res.string.frontier_preview_changes))
            }
        }
    }
}

@Composable
internal fun FrontierTradeOffsCard(
    uiState: FrontierUiState,
    onUiEvent: (FrontierUiEvent) -> Unit,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val expandedText = stringResource(Res.string.frontier_expanded)
    val collapsedText = stringResource(Res.string.frontier_collapsed)
    val a11yText = stringResource(Res.string.frontier_plot_a11y, if (expanded) expandedText else collapsedText)
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("frontier-trade-offs-toggle")
                .semantics {
                    contentDescription = a11yText
                }
                .clickable(
                    role = Role.Button,
                    onClickLabel = if (expanded) stringResource(Res.string.frontier_hide_tradeoffs) else stringResource(Res.string.frontier_tradeoff_title),
                ) { expanded = !expanded },
            color = AppTheme.colors.surfaceContainer,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, AppTheme.colors.outline.copy(alpha = 0.35f)),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(stringResource(Res.string.frontier_tradeoff_title), style = AppTheme.typography.h6)
                    Text(
                        stringResource(Res.string.frontier_tradeoffs_subtitle),
                        color = AppTheme.colors.text.secondary,
                        style = AppTheme.typography.bodySmall,
                    )
                }
                Text(
                    text = if (expanded) stringResource(Res.string.frontier_hide) else stringResource(Res.string.frontier_compare),
                    color = AppTheme.colors.primary,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
        if (expanded) {
            FrontierPlot(
                actions = uiState.actions,
                horizonDays = uiState.horizonDays,
                selectedActionId = uiState.selectedActionId,
                onActionSelected = { onUiEvent(FrontierUiEvent.ActionSelected(it)) },
            )
        }
    }
}

@Composable
internal fun ActivePlanPinnedCard(plan: ActivePlanSummaryUiState, onOpen: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("frontier-active-plan"),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.alternative),
        border = BorderStroke(1.dp, AppTheme.colors.status.success),
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                if (plan.status == PlanStatus.COMPLETED) {
                    stringResource(Res.string.frontier_plan_result_to_confirm)
                } else {
                    val status = when (plan.status) {
                        PlanStatus.PLANNED -> stringResource(Res.string.frontier_plan_status_planned)
                        PlanStatus.IN_PROGRESS -> stringResource(Res.string.frontier_plan_status_in_progress)
                        PlanStatus.PAUSED -> stringResource(Res.string.frontier_plan_status_paused)
                        PlanStatus.COMPLETED -> stringResource(Res.string.frontier_plan_result_to_confirm)
                    }
                    stringResource(Res.string.frontier_plan_current, status)
                },
                color = AppTheme.colors.status.success,
                fontWeight = FontWeight.Bold,
            )
            Text(plan.title, color = AppTheme.colors.onAlternative, style = AppTheme.typography.h6)
            Button(modifier = Modifier.fillMaxWidth().testTag("frontier-open-active-plan"), onClick = onOpen) {
                Text(
                    when (plan.status) {
                        PlanStatus.COMPLETED -> stringResource(Res.string.frontier_plan_open_to_confirm)
                        PlanStatus.IN_PROGRESS -> stringResource(Res.string.frontier_plan_continue)
                        PlanStatus.PAUSED -> stringResource(Res.string.frontier_plan_review_paused)
                        PlanStatus.PLANNED -> stringResource(Res.string.frontier_plan_open)
                    },
                )
            }
        }
    }
}

@Composable
internal fun NoFeasibleActionCard(exclusions: List<FrontierExcludedActionUiState>) {
    Card(
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surfaceContainer),
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = if (exclusions.isEmpty()) stringResource(Res.string.frontier_no_reliable_move) else stringResource(Res.string.frontier_no_fit),
                style = AppTheme.typography.h6,
            )
            Text(
                text = if (exclusions.isEmpty()) {
                    stringResource(Res.string.frontier_no_reliable_help)
                } else {
                    stringResource(Res.string.frontier_no_feasible_help)
                },
                color = AppTheme.colors.text.secondary,
                style = AppTheme.typography.bodyLarge,
            )
        }
    }
}

internal fun FrontierUiState.alternativeFamilies(): List<FrontierActionFamilyUiState> {
    val recommendedId = actions.firstOrNull()?.id
    return actionFamilies.mapNotNull { family ->
        family.copy(actions = family.actions.filterNot { it.id == recommendedId })
            .takeIf { it.actions.isNotEmpty() }
    }
}

@Composable
internal fun FrontierActionUiState.impactSummary(): String = stringResource(
    Res.string.frontier_impact_summary,
    pluralStringResource(Res.plurals.frontier_impact_opportunities, improvedCount, improvedCount),
    pluralStringResource(Res.plurals.frontier_impact_assessed_checks, changedCheckCount, changedCheckCount),
)

@Composable
internal fun FrontierActionUiState.rippleNodes(): List<ActionImpactNodeUiState> {
    val deadlineConfirmation = stringResource(Res.string.frontier_deadline_confirmation)
    val passedLabel = stringResource(Res.string.frontier_ripple_passed)
    val progressLabel = stringResource(Res.string.frontier_ripple_progress)
    val verificationLabel = stringResource(Res.string.frontier_ripple_verification)
    val nodes = mutableListOf<ActionImpactNodeUiState>()
    for (door in doorComparisons) {
        val outcome = when {
            door.isNewlyOpen -> ImpactOutcome.CURRENT_CHECKS_PASSED
            door.needsVerification -> ImpactOutcome.NEEDS_VERIFICATION
            else -> ImpactOutcome.CHECKED_PROGRESS
        }
        nodes += ActionImpactNodeUiState(
            id = door.id,
            title = door.title,
            statusLabel = when (outcome) {
                ImpactOutcome.CURRENT_CHECKS_PASSED -> passedLabel
                ImpactOutcome.CHECKED_PROGRESS -> progressLabel
                ImpactOutcome.NEEDS_VERIFICATION -> verificationLabel
            },
            transitionLabel = "${door.actualText()} → ${door.previewText()}",
            outcome = outcome,
            timingWarning = if (door.requiresDeadlineCheck) deadlineConfirmation else null,
        )
    }
    return nodes
}

@Composable
internal fun FrontierMoveKind.label(): String = when (this) {
    FrontierMoveKind.VERIFY -> stringResource(Res.string.frontier_kind_verify)
    FrontierMoveKind.IMPROVE -> stringResource(Res.string.frontier_kind_improve)
    FrontierMoveKind.PREPARE -> stringResource(Res.string.frontier_kind_prepare)
}

@Composable
internal fun FrontierMoveKind.suggestionLabel(): String = when (this) {
    FrontierMoveKind.VERIFY -> stringResource(Res.string.frontier_suggestion_verify)
    FrontierMoveKind.IMPROVE -> stringResource(Res.string.frontier_suggestion_improve)
    FrontierMoveKind.PREPARE -> stringResource(Res.string.frontier_suggestion_prepare)
}

@Composable
internal fun FrontierMoveKind.description(): String = when (this) {
    FrontierMoveKind.VERIFY -> stringResource(Res.string.frontier_verify_description)
    FrontierMoveKind.IMPROVE -> stringResource(Res.string.frontier_improve_description)
    FrontierMoveKind.PREPARE -> stringResource(Res.string.frontier_prepare_description)
}

@Composable
internal fun ExcludedActionsCard(exclusions: List<FrontierExcludedActionUiState>) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("frontier-exclusions"),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surfaceContainer),
        border = BorderStroke(1.dp, AppTheme.colors.outline.copy(alpha = 0.35f)),
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(stringResource(Res.string.frontier_outside_limits, exclusions.size), style = AppTheme.typography.h6)
                Text(
                    text = stringResource(Res.string.frontier_exclusions_notice),
                    color = AppTheme.colors.text.secondary,
                    style = AppTheme.typography.bodySmall,
                )
            }
            exclusions.forEach { exclusion ->
                Column(
                    modifier = Modifier.fillMaxWidth().testTag("frontier-exclusion-${exclusion.id}"),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(exclusion.titleText(), fontWeight = FontWeight.SemiBold)
                    val reasonCount = if (exclusion.reasonCodes.isEmpty()) exclusion.reasons.size else exclusion.reasonCodes.size
                    repeat(reasonCount) { index ->
                        val reason = if (exclusion.reasonCodes.isEmpty()) exclusion.reasons[index] else exclusion.reasonCodes[index].reasonText()
                        Text(
                            text = "• $reason",
                            color = AppTheme.colors.text.secondary,
                            style = AppTheme.typography.bodySmall,
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun NoSelectionCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surfaceContainer),
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(stringResource(Res.string.frontier_no_compare), style = AppTheme.typography.h5)
            Text(
                text = stringResource(Res.string.frontier_adjust_limits),
                color = AppTheme.colors.text.secondary,
                style = AppTheme.typography.bodyLarge,
            )
        }
    }
}
