package io.github.vietdsk6.porta.presentation.screens.activeplan

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.vietdsk6.porta.designsystem.theme.AppTheme
import io.github.vietdsk6.porta.generated.resources.*
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun GoalCard(plan: ActivePlanCardUiState) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("active-plan-card"),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surfaceContainer),
        border = BorderStroke(1.dp, AppTheme.colors.primary.copy(alpha = 0.72f)),
        shape = RoundedCornerShape(topStart = 40.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 40.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(plan.localizedStatus(), color = AppTheme.colors.status.success, fontWeight = FontWeight.Bold)
                Text(plan.localizedUpdated(), color = AppTheme.colors.text.secondary, style = AppTheme.typography.bodySmall)
            }
            Text(plan.title, style = AppTheme.typography.h5)
            Text(
                stringResource(Res.string.active_target, plan.targetLabel),
                color = AppTheme.colors.primary,
                style = AppTheme.typography.h6,
            )
            Text(
                plan.localizedDuration(),
                color = AppTheme.colors.text.secondary,
                style = AppTheme.typography.bodyMedium,
            )
            if (plan.deadlineMisses.isNotEmpty()) {
                Text(
                    pluralStringResource(
                        Res.plurals.active_deadline_concern,
                        plan.deadlineMisses.size,
                        plan.deadlineMisses.size,
                    ),
                    modifier = Modifier.testTag("plan-deadline-warning"),
                    color = AppTheme.colors.status.warning,
                    style = AppTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricPill(plan.localizedEffort(), Modifier.weight(1f))
                MetricPill(plan.localizedCost(), Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun MetricPill(label: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.alternative),
        shape = RoundedCornerShape(12.dp),
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            color = AppTheme.colors.onAlternative,
            style = AppTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
internal fun PlanTimeline(plan: ActivePlanCardUiState) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("plan-status-timeline"),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surfaceContainer),
        shape = RoundedCornerShape(22.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(stringResource(Res.string.active_plan_status), style = AppTheme.typography.h6)
            plan.steps.forEachIndexed { index, step ->
                Row(
                    modifier = Modifier.fillMaxWidth().testTag("plan-step-$index"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Surface(
                        modifier = Modifier.size(32.dp),
                        shape = CircleShape,
                        color = if (step.state == PlanStepState.DONE) {
                            AppTheme.colors.status.success
                        } else {
                            AppTheme.colors.alternative
                        },
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = if (step.state == PlanStepState.DONE) "✓" else "${index + 1}",
                                color = if (step.state == PlanStepState.DONE) {
                                    AppTheme.colors.background
                                } else {
                                    AppTheme.colors.onAlternative
                                },
                                style = AppTheme.typography.bodyMedium,
                            )
                        }
                    }
                    Column {
                        Text(step.localizedTitle(), style = AppTheme.typography.bodyLarge)
                        Text(
                            step.state.label(),
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
private fun PlanStepState.label(): String = when (this) {
    PlanStepState.DONE -> stringResource(Res.string.active_step_done)
    PlanStepState.IN_PROGRESS -> stringResource(Res.string.active_step_progress)
    PlanStepState.PAUSED -> stringResource(Res.string.active_step_paused)
    PlanStepState.NEXT -> stringResource(Res.string.active_step_next)
    PlanStepState.LATER -> stringResource(Res.string.active_step_later)
}

@Composable
internal fun EvidenceAndImpactCard(plan: ActivePlanCardUiState) {
    var showAll by remember(plan.id) { mutableStateOf(false) }
    val candidateCount = plan.candidateOpportunityIds.size
    val notRuledOutCount = plan.notRuledOutOpportunityIds.size
    Card(
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surfaceContainer),
        shape = RoundedCornerShape(22.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(stringResource(Res.string.active_suggested_evidence), style = AppTheme.typography.h6)
            Text(plan.localizedEvidence(), color = AppTheme.colors.text.secondary)
            Text(stringResource(Res.string.active_potential_changes), style = AppTheme.typography.h6)
            if (candidateCount == 0) {
                Text(
                    stringResource(Res.string.active_no_changes),
                    color = AppTheme.colors.text.secondary,
                )
            } else {
                Text(
                    pluralStringResource(Res.plurals.active_candidate_count, candidateCount, candidateCount),
                    modifier = Modifier.testTag("plan-candidate-impact-count"),
                    color = AppTheme.colors.text.primary,
                )
                Text(
                    stringResource(Res.string.active_not_ruled_out, notRuledOutCount),
                    modifier = Modifier.testTag("plan-in-plan-impact-count"),
                    color = AppTheme.colors.text.secondary,
                    style = AppTheme.typography.bodyMedium,
                )
                if (plan.timingUnverified.isNotEmpty()) {
                    val knownCount = plan.knownDeadlineFeasibleOpportunityIds.size
                    val unverifiedCount = plan.timingUnverified.size
                    Text(
                        pluralStringResource(Res.plurals.active_known_deadline_count, knownCount, knownCount) + " · " +
                            pluralStringResource(Res.plurals.active_unverified_timing_count, unverifiedCount, unverifiedCount),
                        color = AppTheme.colors.status.warning,
                        style = AppTheme.typography.bodySmall,
                    )
                }
                plan.candidateCheckChanges.take(if (showAll) candidateCount else 3).forEach { impact ->
                    Column(Modifier.fillMaxWidth().testTag("plan-impact-${impact.opportunityId}")) {
                        Text(impact.title, style = AppTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Text(
                            impact.timing.label(),
                            color = if (impact.timing == PlanTimingStatus.MISSES_KNOWN_DEADLINE) {
                                AppTheme.colors.status.warning
                            } else {
                                AppTheme.colors.text.secondary
                            },
                            style = AppTheme.typography.bodySmall,
                        )
                    }
                }
                if (candidateCount > 3) {
                    TextButton(
                        modifier = Modifier.testTag("plan-impact-expand"),
                        onClick = { showAll = !showAll },
                    ) {
                        Text(
                            if (showAll) {
                                stringResource(Res.string.active_show_fewer)
                            } else {
                                stringResource(Res.string.active_see_all_count, candidateCount)
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlanTimingStatus.label(): String = when (this) {
    PlanTimingStatus.WITHIN_KNOWN_DEADLINE -> stringResource(Res.string.active_timing_fit)
    PlanTimingStatus.TIMING_UNVERIFIED -> stringResource(Res.string.active_timing_verify)
    PlanTimingStatus.MISSES_KNOWN_DEADLINE -> stringResource(Res.string.active_timing_miss)
    PlanTimingStatus.OUTSIDE_CURRENT_CYCLE -> stringResource(Res.string.active_timing_outside)
}

@Composable
internal fun RiskCard(riskNote: String) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surfaceContainer),
        shape = RoundedCornerShape(18.dp),
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            TextButton(modifier = Modifier.testTag("plan-risk-expand"), onClick = { expanded = !expanded }) {
                Text(if (expanded) stringResource(Res.string.active_hide_assumptions) else stringResource(Res.string.active_show_assumptions))
            }
            if (expanded) Text(riskNote, color = AppTheme.colors.text.secondary, style = AppTheme.typography.bodyMedium)
        }
    }
}

@Composable
internal fun CompletionBoundaryCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.alternative),
        shape = RoundedCornerShape(18.dp),
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(stringResource(Res.string.active_result_pending), color = AppTheme.colors.primary, fontWeight = FontWeight.Bold)
            Text(
                stringResource(Res.string.active_result_pending_help),
                color = AppTheme.colors.onAlternative,
                style = AppTheme.typography.bodyMedium,
            )
        }
    }
}
