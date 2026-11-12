package io.github.vietdsk6.porta.presentation.screens.frontier

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.vietdsk6.porta.designsystem.theme.AppTheme
import io.github.vietdsk6.porta.generated.resources.Res
import io.github.vietdsk6.porta.generated.resources.frontier_deadline_confirmation
import io.github.vietdsk6.porta.generated.resources.frontier_desktop_actual_profile
import io.github.vietdsk6.porta.generated.resources.frontier_desktop_header_actual
import io.github.vietdsk6.porta.generated.resources.frontier_desktop_header_door
import io.github.vietdsk6.porta.generated.resources.frontier_desktop_header_simulated
import io.github.vietdsk6.porta.generated.resources.frontier_desktop_overflow_comparison
import io.github.vietdsk6.porta.generated.resources.frontier_desktop_progress_count
import io.github.vietdsk6.porta.generated.resources.frontier_desktop_saved_door
import io.github.vietdsk6.porta.generated.resources.frontier_desktop_what_if_preview
import io.github.vietdsk6.porta.generated.resources.frontier_preview_changes
import io.github.vietdsk6.porta.generated.resources.frontier_preview_step_hint
import io.github.vietdsk6.porta.generated.resources.frontier_simulation_notice
import io.github.vietdsk6.porta.generated.resources.frontier_simulation_preview
import io.github.vietdsk6.porta.generated.resources.frontier_why_rank
import io.github.vietdsk6.porta.presentation.components.porta.ActionRipple
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun WideFrontierContent(
    uiState: FrontierUiState,
    onUiEvent: (FrontierUiEvent) -> Unit,
    onPreviewScenario: (String) -> Unit,
    onOpenActivePlan: () -> Unit,
) {
    val selectedAction = uiState.actions.firstOrNull { it.id == uiState.selectedActionId }
        ?: uiState.actions.firstOrNull()
    val recommendedAction = uiState.actions.firstOrNull()
    val alternativeFamilies = uiState.alternativeFamilies()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 1440.dp)
                .testTag("frontier-desktop-workspace"),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxHeight().weight(0.4f).testTag("frontier-master-list"),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                item { FrontierIntroduction(uiState = uiState) }
                uiState.activePlan?.takeIf { it.needsPriorityPlacement }?.let { plan ->
                    item { ActivePlanPinnedCard(plan = plan, onOpen = onOpenActivePlan) }
                }
                recommendedAction?.let { action ->
                    item {
                        FrontierCard(
                            action = action,
                            eyebrow = action.kind.suggestionLabel(),
                            isRecommended = true,
                            isSelected = action.id == selectedAction?.id,
                            isSelectable = true,
                            showExplanation = true,
                            onSelect = { onUiEvent(FrontierUiEvent.ActionSelected(action.id)) },
                            onPreview = { onPreviewScenario(action.id) },
                        )
                    }
                }
                uiState.suggestedGrowthHorizonDays?.let { horizonDays ->
                    item {
                        GrowthHorizonPrompt(
                            currentHorizonDays = uiState.horizonDays,
                            suggestedHorizonDays = horizonDays,
                            onUseHorizon = { onUiEvent(FrontierUiEvent.HorizonChanged(horizonDays)) },
                        )
                    }
                }
                uiState.activePlan?.takeUnless { it.needsPriorityPlacement }?.let { plan ->
                    item { ActivePlanPinnedCard(plan = plan, onOpen = onOpenActivePlan) }
                }
                item {
                    FrontierControls(
                        uiState = uiState,
                        onUiEvent = onUiEvent,
                        collapsible = false,
                    )
                }
                if (uiState.actions.isEmpty()) {
                    item { NoFeasibleActionCard(uiState.exclusions) }
                } else {
                    items(alternativeFamilies, key = FrontierActionFamilyUiState::id) { family ->
                        AlternativeFamilySection(
                            family = family,
                            selectedActionId = selectedAction?.id,
                            onSelect = { actionId ->
                                onUiEvent(FrontierUiEvent.ActionSelected(actionId))
                            },
                            onPreview = onPreviewScenario,
                        )
                    }
                }
                if (uiState.exclusions.isNotEmpty()) {
                    item { ExcludedActionsCard(uiState.exclusions) }
                }
                item { Spacer(Modifier.height(8.dp)) }
            }

            LazyColumn(
                modifier = Modifier.fillMaxHeight().weight(0.6f).testTag("frontier-detail-pane"),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                if (selectedAction == null) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            DesktopCompareSelector(
                                actions = uiState.actions,
                                comparedActionIds = uiState.comparedActionIds,
                                onToggle = { onUiEvent(FrontierUiEvent.ComparisonToggled(it)) },
                                onClear = { onUiEvent(FrontierUiEvent.ComparisonCleared) },
                            )
                            NoSelectionCard()
                        }
                    }
                } else if (uiState.comparedActions.size >= 2) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            DesktopCompareSelector(
                                actions = uiState.actions,
                                comparedActionIds = uiState.comparedActionIds,
                                onToggle = { onUiEvent(FrontierUiEvent.ComparisonToggled(it)) },
                                onClear = { onUiEvent(FrontierUiEvent.ComparisonCleared) },
                            )
                            DesktopScenarioCompareLab(actions = uiState.comparedActions)
                        }
                    }
                } else {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            DesktopCompareSelector(
                                actions = uiState.actions,
                                comparedActionIds = uiState.comparedActionIds,
                                onToggle = { onUiEvent(FrontierUiEvent.ComparisonToggled(it)) },
                                onClear = { onUiEvent(FrontierUiEvent.ComparisonCleared) },
                            )
                            FrontierComparisonPanel(
                                action = selectedAction,
                                onOpenFullPreview = { onPreviewScenario(selectedAction.id) },
                            )
                        }
                    }
                    if (uiState.hasMeaningfulTradeOffs) {
                        item {
                            FrontierPlot(
                                actions = uiState.actions,
                                horizonDays = uiState.horizonDays,
                                selectedActionId = selectedAction.id,
                                onActionSelected = { onUiEvent(FrontierUiEvent.ActionSelected(it)) },
                            )
                        }
                    }
                    item { Spacer(Modifier.height(8.dp)) }
                }
            }
        }
    }
}

@Composable
internal fun FrontierComparisonPanel(
    action: FrontierActionUiState,
    onOpenFullPreview: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("frontier-comparison-${action.id}"),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surfaceContainer),
        border = BorderStroke(1.dp, AppTheme.colors.outline.copy(alpha = 0.45f)),
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(stringResource(Res.string.frontier_simulation_preview), color = AppTheme.colors.primary, fontWeight = FontWeight.ExtraBold)
                Text(action.titleText(), style = AppTheme.typography.h4, color = AppTheme.colors.text.primary)
                Text(
                    text = stringResource(Res.string.frontier_simulation_notice),
                    style = AppTheme.typography.bodyMedium,
                    color = AppTheme.colors.text.secondary,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                ValueComparisonCard(
                    eyebrow = stringResource(Res.string.frontier_desktop_actual_profile),
                    value = action.currentText(),
                    modifier = Modifier.weight(1f),
                )
                ValueComparisonCard(
                    eyebrow = stringResource(Res.string.frontier_desktop_what_if_preview),
                    value = action.targetText(),
                    modifier = Modifier.weight(1f),
                    emphasized = true,
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(stringResource(Res.string.frontier_why_rank), style = AppTheme.typography.h6, color = AppTheme.colors.text.primary)
                Text(
                    text = action.explanationCode?.toText(action.kind) ?: action.explanation,
                    style = AppTheme.typography.bodyMedium,
                    color = AppTheme.colors.text.secondary,
                )
            }

            if (action.doorComparisons.isNotEmpty()) {
                ActionRipple(
                    actionLabel = action.titleText(),
                    actualLabel = action.currentText(),
                    simulatedLabel = action.targetText(),
                    nodes = action.rippleNodes(),
                    overflowDestination = stringResource(Res.string.frontier_desktop_overflow_comparison),
                )
            }

            HorizontalDivider(color = AppTheme.colors.outline.copy(alpha = 0.35f))
            Text(
                text = stringResource(Res.string.frontier_desktop_progress_count, action.doorComparisons.size),
                style = AppTheme.typography.h6,
                color = AppTheme.colors.text.primary,
            )
            DoorComparisonHeader()
            action.doorComparisons.forEach { door ->
                DoorComparisonRow(door)
            }
            if (action.doorComparisons.isEmpty()) {
                Text(
                    text = stringResource(Res.string.frontier_preview_step_hint),
                    style = AppTheme.typography.bodyMedium,
                    color = AppTheme.colors.text.secondary,
                )
            }
            Button(
                modifier = Modifier.fillMaxWidth().testTag("frontier-open-preview-${action.id}"),
                onClick = onOpenFullPreview,
            ) {
                Text(stringResource(Res.string.frontier_preview_changes))
            }
        }
    }
}

@Composable
internal fun ValueComparisonCard(
    eyebrow: String,
    value: String,
    modifier: Modifier = Modifier,
    emphasized: Boolean = false,
) {
    Surface(
        modifier = modifier,
        color = if (emphasized) AppTheme.colors.alternative else AppTheme.colors.background,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = 1.dp,
            color = if (emphasized) AppTheme.colors.primary else AppTheme.colors.outline.copy(alpha = 0.35f),
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = eyebrow,
                style = AppTheme.typography.bodySmall,
                color = AppTheme.colors.primary,
                fontWeight = FontWeight.ExtraBold,
            )
            Text(
                text = value,
                style = AppTheme.typography.h5,
                color = AppTheme.colors.text.primary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
internal fun DoorComparisonHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ComparisonHeaderLabel(stringResource(Res.string.frontier_desktop_header_door), Modifier.weight(1.4f))
        ComparisonHeaderLabel(stringResource(Res.string.frontier_desktop_header_actual), Modifier.weight(0.8f))
        ComparisonHeaderLabel(stringResource(Res.string.frontier_desktop_header_simulated), Modifier.weight(0.8f))
    }
}

@Composable
internal fun ComparisonHeaderLabel(text: String, modifier: Modifier) {
    Text(
        text = text,
        modifier = modifier,
        style = AppTheme.typography.bodySmall,
        color = AppTheme.colors.text.secondary,
        fontWeight = FontWeight.Bold,
    )
}

@Composable
internal fun DoorComparisonRow(door: FrontierDoorComparisonUiState) {
    Column(
        modifier = Modifier.fillMaxWidth().testTag("frontier-door-comparison-${door.id}"),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = door.title,
                modifier = Modifier.weight(1.4f),
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.colors.text.primary,
                fontWeight = FontWeight.SemiBold,
            )
            StateLabel(
                label = door.actualText(),
                modifier = Modifier.weight(0.8f),
                highlighted = false,
            )
            StateLabel(
                label = door.previewText(),
                modifier = Modifier.weight(0.8f),
                highlighted = door.isNewlyOpen,
            )
        }
        if (door.isSaved || door.requiresDeadlineCheck) {
            val savedLabel = stringResource(Res.string.frontier_desktop_saved_door)
            val deadlineLabel = stringResource(Res.string.frontier_deadline_confirmation)
            Text(
                text = buildList {
                    if (door.isSaved) add(savedLabel)
                    if (door.requiresDeadlineCheck) add(deadlineLabel)
                }.joinToString(" · "),
                style = AppTheme.typography.bodySmall,
                color = if (door.requiresDeadlineCheck) {
                    AppTheme.colors.status.warning
                } else {
                    AppTheme.colors.primary
                },
            )
        }
        HorizontalDivider(color = AppTheme.colors.outline.copy(alpha = 0.2f))
    }
}

@Composable
internal fun StateLabel(
    label: String,
    modifier: Modifier = Modifier,
    highlighted: Boolean,
) {
    Surface(
        modifier = modifier,
        color = if (highlighted) {
            AppTheme.colors.status.successContainer
        } else {
            AppTheme.colors.background
        },
        shape = RoundedCornerShape(10.dp),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 7.dp),
            style = AppTheme.typography.bodySmall,
            color = if (highlighted) AppTheme.colors.status.success else AppTheme.colors.text.secondary,
            fontWeight = if (highlighted) FontWeight.Bold else FontWeight.Medium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
internal fun MetricPill(label: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.alternative),
        shape = RoundedCornerShape(12.dp),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            color = AppTheme.colors.onAlternative,
            style = AppTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
