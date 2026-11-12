package io.github.vietdsk6.porta.presentation.screens.frontier

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.vietdsk6.porta.designsystem.theme.AppTheme
import io.github.vietdsk6.porta.generated.resources.Res
import io.github.vietdsk6.porta.generated.resources.frontier_affected_doors
import io.github.vietdsk6.porta.generated.resources.frontier_hide
import io.github.vietdsk6.porta.generated.resources.frontier_hide_reasoning
import io.github.vietdsk6.porta.generated.resources.frontier_hide_ripple
import io.github.vietdsk6.porta.generated.resources.frontier_horizon_compare
import io.github.vietdsk6.porta.generated.resources.frontier_horizon_long
import io.github.vietdsk6.porta.generated.resources.frontier_horizon_use
import io.github.vietdsk6.porta.generated.resources.frontier_metric_cost
import io.github.vietdsk6.porta.generated.resources.frontier_metric_effort
import io.github.vietdsk6.porta.generated.resources.frontier_metric_time
import io.github.vietdsk6.porta.generated.resources.frontier_overflow_preview
import io.github.vietdsk6.porta.generated.resources.frontier_preview_changes
import io.github.vietdsk6.porta.generated.resources.frontier_timing_caveat
import io.github.vietdsk6.porta.generated.resources.frontier_timing_confirmation
import io.github.vietdsk6.porta.generated.resources.frontier_view_ripple
import io.github.vietdsk6.porta.generated.resources.frontier_why_move
import io.github.vietdsk6.porta.presentation.components.porta.ActionRipple
import io.github.vietdsk6.porta.presentation.theme.PortaBreakpoints
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun FrontierCard(
    action: FrontierActionUiState,
    eyebrow: String,
    isRecommended: Boolean,
    isSelected: Boolean,
    isSelectable: Boolean,
    showExplanation: Boolean,
    onSelect: () -> Unit,
    onPreview: () -> Unit,
) {
    var detailsVisible by rememberSaveable(action.id) { mutableStateOf(false) }
    Card(
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
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                AppTheme.colors.alternative
            } else {
                AppTheme.colors.surfaceContainer
            },
        ),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected || isRecommended) {
                AppTheme.colors.primary
            } else {
                AppTheme.colors.outline.copy(alpha = 0.35f)
            },
        ),
        shape = if (isRecommended) {
            RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
        } else {
            RoundedCornerShape(16.dp)
        },
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(11.dp),
        ) {
            Text(
                text = eyebrow,
                color = AppTheme.colors.primary,
                fontWeight = FontWeight.Bold,
            )
            Text(action.titleText(), style = AppTheme.typography.h5)
            Text(
                text = action.scopeText(),
                color = AppTheme.colors.text.secondary,
                style = AppTheme.typography.bodySmall,
            )
            Text(
                text = action.impactSummary(),
                color = AppTheme.colors.status.success,
                style = AppTheme.typography.h6,
            )
            if (action.kind == FrontierMoveKind.VERIFY) {
                Text(
                    text = stringResource(Res.string.frontier_timing_caveat),
                    color = AppTheme.colors.text.secondary,
                    style = AppTheme.typography.bodySmall,
                )
            } else {
                AdaptiveMetricPills(action)
            }
            if (action.timingUnverifiedCount > 0) {
                Text(
                    pluralStringResource(Res.plurals.frontier_timing_confirmation, action.timingUnverifiedCount, action.timingUnverifiedCount),
                    color = AppTheme.colors.status.warning,
                    style = AppTheme.typography.bodySmall,
                )
            }
            action.earliestDeadlineText()?.let { deadlineLabel ->
                Text(
                    text = deadlineLabel,
                    modifier = Modifier.testTag("frontier-earliest-deadline-${action.id}"),
                    color = AppTheme.colors.text.secondary,
                    style = AppTheme.typography.bodySmall,
                )
            }
            if (showExplanation && detailsVisible) {
                Text(
                    text = action.explanationCode?.toText(action.kind) ?: action.explanation,
                    color = AppTheme.colors.text.secondary,
                    style = AppTheme.typography.bodyMedium,
                )
                Text(
                    text = stringResource(Res.string.frontier_affected_doors, action.doorTitles.joinToString(separator = " · ")),
                    style = AppTheme.typography.bodyMedium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    if (action.kind != FrontierMoveKind.VERIFY) {
                        Text(
                            text = action.estimateSourceText(),
                            color = AppTheme.colors.text.secondary,
                            style = AppTheme.typography.bodySmall,
                        )
                    }
                    Text(
                        text = action.impactBasisText(),
                        color = AppTheme.colors.text.secondary,
                        style = AppTheme.typography.bodySmall,
                    )
                }
            }
            if (showExplanation) {
                TextButton(
                    modifier = Modifier.align(Alignment.End).testTag("frontier-action-details-${action.id}"),
                    onClick = { detailsVisible = !detailsVisible },
                ) {
                    Text(if (detailsVisible) stringResource(Res.string.frontier_hide_reasoning) else stringResource(Res.string.frontier_why_move))
                }
            }
            Button(modifier = Modifier.fillMaxWidth(), onClick = onPreview) {
                Text(stringResource(Res.string.frontier_preview_changes))
            }
        }
    }
}

@Composable
internal fun RecommendedRippleDisclosure(action: FrontierActionUiState) {
    var expanded by rememberSaveable(action.id) { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        TextButton(
            modifier = Modifier.testTag("frontier-ripple-toggle"),
            onClick = { expanded = !expanded },
        ) {
            Text(if (expanded) stringResource(Res.string.frontier_hide_ripple) else stringResource(Res.string.frontier_view_ripple))
        }
        if (expanded) {
            ActionRipple(
                actionLabel = action.titleText(),
                actualLabel = action.currentText(),
                simulatedLabel = action.targetText(),
                nodes = action.rippleNodes(),
                overflowDestination = stringResource(Res.string.frontier_overflow_preview),
            )
        }
    }
}

@Composable
internal fun GrowthHorizonPrompt(
    currentHorizonDays: Int,
    suggestedHorizonDays: Int,
    onUseHorizon: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth().testTag("frontier-growth-horizon-prompt"),
        color = AppTheme.colors.alternative,
        shape = RoundedCornerShape(14.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(stringResource(Res.string.frontier_horizon_long, currentHorizonDays), style = AppTheme.typography.h6)
            Text(
                text = stringResource(Res.string.frontier_horizon_compare, suggestedHorizonDays),
                color = AppTheme.colors.text.secondary,
                style = AppTheme.typography.bodyMedium,
            )
            TextButton(
                modifier = Modifier.align(Alignment.End).testTag("frontier-use-$suggestedHorizonDays-days"),
                onClick = onUseHorizon,
            ) {
                Text(stringResource(Res.string.frontier_horizon_use, suggestedHorizonDays))
            }
        }
    }
}

@Composable
internal fun AdaptiveMetricPills(action: FrontierActionUiState) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val stackMetrics = maxWidth < 260.dp ||
            LocalDensity.current.fontScale >= PortaBreakpoints.maxWideFontScale
        if (stackMetrics) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricPill(stringResource(Res.string.frontier_metric_time, action.durationText()), Modifier.fillMaxWidth())
                MetricPill(stringResource(Res.string.frontier_metric_effort, action.effortText()), Modifier.fillMaxWidth())
                MetricPill(stringResource(Res.string.frontier_metric_cost, action.costText()), Modifier.fillMaxWidth())
            }
        } else if (maxWidth < 430.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricPill(stringResource(Res.string.frontier_metric_time, action.durationText()), Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetricPill(stringResource(Res.string.frontier_metric_effort, action.effortText()), Modifier.weight(1f))
                    MetricPill(stringResource(Res.string.frontier_metric_cost, action.costText()), Modifier.weight(1f))
                }
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricPill(stringResource(Res.string.frontier_metric_time, action.durationText()), Modifier.weight(1f))
                MetricPill(stringResource(Res.string.frontier_metric_effort, action.effortText()), Modifier.weight(1f))
                MetricPill(stringResource(Res.string.frontier_metric_cost, action.costText()), Modifier.weight(1f))
            }
        }
    }
}
