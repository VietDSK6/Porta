package io.github.vietdsk6.porta.presentation.screens.frontier

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.vietdsk6.porta.designsystem.theme.AppTheme
import io.github.vietdsk6.porta.domain.model.EligibilityState
import io.github.vietdsk6.porta.generated.resources.Res
import io.github.vietdsk6.porta.generated.resources.frontier_actual
import io.github.vietdsk6.porta.generated.resources.frontier_clear
import io.github.vietdsk6.porta.generated.resources.frontier_compare
import io.github.vietdsk6.porta.generated.resources.frontier_compare_actual_door
import io.github.vietdsk6.porta.generated.resources.frontier_compare_actual_profile
import io.github.vietdsk6.porta.generated.resources.frontier_compare_add_a11y
import io.github.vietdsk6.porta.generated.resources.frontier_compare_baseline
import io.github.vietdsk6.porta.generated.resources.frontier_compare_change
import io.github.vietdsk6.porta.generated.resources.frontier_compare_choose_one
import io.github.vietdsk6.porta.generated.resources.frontier_compare_choose_two
import io.github.vietdsk6.porta.generated.resources.frontier_compare_deadline_uncertain
import io.github.vietdsk6.porta.generated.resources.frontier_compare_improved_check
import io.github.vietdsk6.porta.generated.resources.frontier_compare_independence_note
import io.github.vietdsk6.porta.generated.resources.frontier_compare_lab
import io.github.vietdsk6.porta.generated.resources.frontier_compare_max
import io.github.vietdsk6.porta.generated.resources.frontier_compare_metrics
import io.github.vietdsk6.porta.generated.resources.frontier_compare_needs_verification
import io.github.vietdsk6.porta.generated.resources.frontier_compare_newly_open
import io.github.vietdsk6.porta.generated.resources.frontier_compare_newly_passed
import io.github.vietdsk6.porta.generated.resources.frontier_compare_no_change
import io.github.vietdsk6.porta.generated.resources.frontier_compare_no_deadline_uncertainty
import io.github.vietdsk6.porta.generated.resources.frontier_compare_progress
import io.github.vietdsk6.porta.generated.resources.frontier_compare_remove_a11y
import io.github.vietdsk6.porta.generated.resources.frontier_compare_select_help
import io.github.vietdsk6.porta.generated.resources.frontier_compare_selected
import io.github.vietdsk6.porta.generated.resources.frontier_compare_timing_count
import io.github.vietdsk6.porta.generated.resources.frontier_compare_vs
import io.github.vietdsk6.porta.generated.resources.frontier_independent_comparison
import io.github.vietdsk6.porta.generated.resources.frontier_opportunity_checks
import io.github.vietdsk6.porta.generated.resources.frontier_saved_profile
import io.github.vietdsk6.porta.generated.resources.frontier_what_if
import io.github.vietdsk6.porta.presentation.components.trustLabelText
import io.github.vietdsk6.porta.presentation.theme.PortaBreakpoints
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun DesktopCompareSelector(
    actions: List<FrontierActionUiState>,
    comparedActionIds: List<String>,
    onToggle: (String) -> Unit,
    onClear: () -> Unit,
) {
    if (actions.size < 2) return
    val selectionIsFull = comparedActionIds.size >= MAX_COMPARED_ACTIONS
    Card(
        modifier = Modifier.fillMaxWidth().testTag("frontier-compare-selector"),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surfaceContainer),
        border = BorderStroke(1.dp, AppTheme.colors.outline.copy(alpha = 0.45f)),
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(stringResource(Res.string.frontier_compare_lab), color = AppTheme.colors.primary, fontWeight = FontWeight.ExtraBold)
                    Text(
                        text = stringResource(Res.string.frontier_compare_select_help),
                        color = AppTheme.colors.text.secondary,
                        style = AppTheme.typography.bodySmall,
                    )
                }
                if (comparedActionIds.isNotEmpty()) {
                    TextButton(
                        modifier = Modifier.testTag("frontier-compare-clear"),
                        onClick = onClear,
                    ) {
                        Text(stringResource(Res.string.frontier_clear))
                    }
                }
            }
            actions.forEach { action ->
                val selected = action.id in comparedActionIds
                val removeDescription = stringResource(Res.string.frontier_compare_remove_a11y, action.titleText())
                val addDescription = stringResource(Res.string.frontier_compare_add_a11y, action.titleText())
                FilterChip(
                    selected = selected,
                    onClick = { onToggle(action.id) },
                    label = {
                        Text(
                            text = action.titleText(),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("frontier-compare-toggle-${action.id}")
                        .semantics {
                            contentDescription = if (selected) {
                                removeDescription
                            } else {
                                addDescription
                            }
                        },
                    enabled = selected || !selectionIsFull,
                )
            }
            Text(
                text = when (comparedActionIds.size) {
                    0 -> stringResource(Res.string.frontier_compare_choose_two)
                    1 -> stringResource(Res.string.frontier_compare_choose_one)
                    MAX_COMPARED_ACTIONS -> stringResource(Res.string.frontier_compare_max)
                    else -> stringResource(Res.string.frontier_compare_selected, comparedActionIds.size)
                },
                modifier = Modifier.testTag("frontier-compare-selection-status"),
                color = AppTheme.colors.text.secondary,
                style = AppTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
internal fun DesktopScenarioCompareLab(actions: List<FrontierActionUiState>) {
    val comparisonActions = actions.take(MAX_COMPARED_ACTIONS)
    if (comparisonActions.size < 2) return
    val doorRows = comparisonDoorRows(comparisonActions)
    Card(
        modifier = Modifier.fillMaxWidth().testTag("frontier-scenario-compare-lab"),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surfaceContainer),
        border = BorderStroke(1.dp, AppTheme.colors.primary.copy(alpha = 0.55f)),
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(stringResource(Res.string.frontier_independent_comparison), color = AppTheme.colors.primary, fontWeight = FontWeight.ExtraBold)
                Text(stringResource(Res.string.frontier_compare_vs, comparisonActions.size), style = AppTheme.typography.h4)
                Text(
                    text = stringResource(Res.string.frontier_compare_independence_note),
                    modifier = Modifier.testTag("frontier-compare-independence-note"),
                    color = AppTheme.colors.text.secondary,
                    style = AppTheme.typography.bodyMedium,
                )
            }
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val stackColumns = maxWidth < 600.dp ||
                    LocalDensity.current.fontScale >= PortaBreakpoints.maxWideFontScale
                if (stackColumns) {
                    StackedScenarioComparison(actions = comparisonActions, doorRows = doorRows)
                } else {
                    ScenarioComparisonMatrix(actions = comparisonActions, doorRows = doorRows)
                }
            }
        }
    }
}

@Composable
internal fun ScenarioComparisonMatrix(
    actions: List<FrontierActionUiState>,
    doorRows: List<DesktopComparisonDoorUiState>,
) {
    Column(
        modifier = Modifier.fillMaxWidth().testTag("frontier-compare-matrix"),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ActualScenarioSummary(modifier = Modifier.weight(1f))
            actions.forEach { action ->
                ActionScenarioSummary(action = action, modifier = Modifier.weight(1f))
            }
        }
        if (doorRows.isNotEmpty()) {
            HorizontalDivider(color = AppTheme.colors.outline.copy(alpha = 0.35f))
            Text(stringResource(Res.string.frontier_opportunity_checks), style = AppTheme.typography.h6)
            doorRows.forEach { door ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("frontier-compare-door-${door.id}"),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    ScenarioDoorCell(
                        label = door.actualState?.trustLabelText() ?: door.actualStateLabel,
                        supportingLabel = stringResource(Res.string.frontier_compare_actual_door, door.title),
                        highlighted = false,
                        modifier = Modifier.weight(1f),
                    )
                    actions.forEach { action ->
                        val change = door.changesByActionId[action.id]
                        ScenarioDoorCell(
                            label = change?.previewText() ?: (door.actualState?.trustLabelText() ?: door.actualStateLabel),
                            supportingLabel = when {
                                change == null -> stringResource(Res.string.frontier_compare_no_change)
                                change.isNewlyOpen -> stringResource(Res.string.frontier_compare_newly_passed)
                                change.needsVerification -> stringResource(Res.string.frontier_compare_needs_verification)
                                else -> stringResource(Res.string.frontier_compare_improved_check)
                            },
                            warningLabel = if (change?.requiresDeadlineCheck == true) stringResource(Res.string.frontier_compare_deadline_uncertain) else null,
                            highlighted = change?.isNewlyOpen == true,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun StackedScenarioComparison(
    actions: List<FrontierActionUiState>,
    doorRows: List<DesktopComparisonDoorUiState>,
) {
    Column(
        modifier = Modifier.fillMaxWidth().testTag("frontier-compare-stacked"),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        ActualScenarioSummary(modifier = Modifier.fillMaxWidth())
        actions.forEach { action ->
            ActionScenarioSummary(action = action, modifier = Modifier.fillMaxWidth())
        }
        if (doorRows.isNotEmpty()) {
            HorizontalDivider(color = AppTheme.colors.outline.copy(alpha = 0.35f))
            Text(stringResource(Res.string.frontier_opportunity_checks), style = AppTheme.typography.h6)
            doorRows.forEach { door ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("frontier-compare-door-${door.id}"),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(door.title, fontWeight = FontWeight.Bold, style = AppTheme.typography.bodyMedium)
                    ScenarioDoorCell(
                        label = door.actualState?.trustLabelText() ?: door.actualStateLabel,
                        supportingLabel = stringResource(Res.string.frontier_compare_actual_profile),
                        highlighted = false,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    actions.forEach { action ->
                        val change = door.changesByActionId[action.id]
                        ScenarioDoorCell(
                            label = change?.previewText() ?: (door.actualState?.trustLabelText() ?: door.actualStateLabel),
                            supportingLabel = stringResource(
                                Res.string.frontier_compare_change,
                                action.titleText(),
                                when {
                                    change == null -> stringResource(Res.string.frontier_compare_no_change)
                                    change.isNewlyOpen -> stringResource(Res.string.frontier_compare_newly_passed)
                                    change.needsVerification -> stringResource(Res.string.frontier_compare_needs_verification)
                                    else -> stringResource(Res.string.frontier_compare_improved_check)
                                },
                            ),
                            warningLabel = if (change?.requiresDeadlineCheck == true) stringResource(Res.string.frontier_compare_deadline_uncertain) else null,
                            highlighted = change?.isNewlyOpen == true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun ActualScenarioSummary(modifier: Modifier) {
    Surface(
        modifier = modifier,
        color = AppTheme.colors.background,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, AppTheme.colors.outline.copy(alpha = 0.35f)),
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(stringResource(Res.string.frontier_actual), color = AppTheme.colors.primary, fontWeight = FontWeight.ExtraBold)
            Text(stringResource(Res.string.frontier_saved_profile), style = AppTheme.typography.h6)
            Text(
                stringResource(Res.string.frontier_compare_baseline),
                color = AppTheme.colors.text.secondary,
                style = AppTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
internal fun ActionScenarioSummary(action: FrontierActionUiState, modifier: Modifier) {
    Surface(
        modifier = modifier.testTag("frontier-compare-scenario-${action.id}"),
        color = AppTheme.colors.alternative,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, AppTheme.colors.primary.copy(alpha = 0.45f)),
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(stringResource(Res.string.frontier_what_if), color = AppTheme.colors.primary, fontWeight = FontWeight.ExtraBold)
            Text(action.titleText(), style = AppTheme.typography.h6, maxLines = 4, overflow = TextOverflow.Ellipsis)
            Text(
                "${action.currentText()} → ${action.targetText()}",
                color = AppTheme.colors.onAlternative,
                style = AppTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                stringResource(Res.string.frontier_compare_progress, action.improvedCount, action.changedCheckCount),
                color = AppTheme.colors.status.success,
                style = AppTheme.typography.bodySmall,
            )
            Text(
                stringResource(Res.string.frontier_compare_newly_open, action.newlyOpenCount),
                color = AppTheme.colors.text.secondary,
                style = AppTheme.typography.bodySmall,
            )
            Text(
                stringResource(Res.string.frontier_compare_metrics, action.durationText(), action.effortText(), action.costText()),
                color = AppTheme.colors.text.secondary,
                style = AppTheme.typography.bodySmall,
            )
            Text(
                text = if (action.timingUnverifiedCount == 0) {
                    stringResource(Res.string.frontier_compare_no_deadline_uncertainty)
                } else {
                    pluralStringResource(Res.plurals.frontier_compare_timing_count, action.timingUnverifiedCount, action.timingUnverifiedCount)
                },
                color = if (action.timingUnverifiedCount == 0) {
                    AppTheme.colors.text.secondary
                } else {
                    AppTheme.colors.status.warning
                },
                style = AppTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
internal fun ScenarioDoorCell(
    label: String,
    supportingLabel: String,
    highlighted: Boolean,
    modifier: Modifier,
    warningLabel: String? = null,
) {
    Surface(
        modifier = modifier,
        color = if (highlighted) AppTheme.colors.status.successContainer else AppTheme.colors.background,
        shape = RoundedCornerShape(10.dp),
    ) {
        Column(modifier = Modifier.padding(9.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text = label,
                color = if (highlighted) AppTheme.colors.status.success else AppTheme.colors.text.primary,
                style = AppTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = supportingLabel,
                color = AppTheme.colors.text.secondary,
                style = AppTheme.typography.bodySmall,
            )
            warningLabel?.let {
                Text(it, color = AppTheme.colors.status.warning, style = AppTheme.typography.bodySmall)
            }
        }
    }
}

internal data class DesktopComparisonDoorUiState(
    val id: String,
    val title: String,
    val actualStateLabel: String,
    val actualState: EligibilityState? = null,
    val changesByActionId: Map<String, FrontierDoorComparisonUiState>,
)

internal fun comparisonDoorRows(actions: List<FrontierActionUiState>): List<DesktopComparisonDoorUiState> {
    val comparisons = actions.flatMap { action -> action.doorComparisons.map { action.id to it } }
    return comparisons
        .distinctBy { (_, comparison) -> comparison.id }
        .map { (_, comparison) ->
            DesktopComparisonDoorUiState(
                id = comparison.id,
                title = comparison.title,
                actualStateLabel = comparison.actualStateLabel,
                actualState = comparison.actualState,
                changesByActionId = comparisons
                    .filter { (_, candidate) -> candidate.id == comparison.id }
                    .associate { (actionId, candidate) -> actionId to candidate },
            )
        }
}
