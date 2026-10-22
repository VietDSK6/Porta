package io.github.vietdsk6.porta.presentation.screens.frontier

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.vietdsk6.porta.designsystem.components.ScreenWithToolbar
import io.github.vietdsk6.porta.designsystem.theme.AppTheme

@Composable
fun FrontierScreen(
    viewModel: FrontierViewModel,
    onPreviewScenario: (String) -> Unit,
    onOpenActivePlan: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    FrontierScreen(
        uiState = uiState,
        onUiEvent = viewModel::onUiEvent,
        onPreviewScenario = onPreviewScenario,
        onOpenActivePlan = onOpenActivePlan,
        modifier = modifier.fillMaxSize(),
    )
}

@Composable
fun FrontierScreen(
    uiState: FrontierUiState,
    onUiEvent: (FrontierUiEvent) -> Unit,
    onPreviewScenario: (String) -> Unit,
    onOpenActivePlan: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ScreenWithToolbar(
        modifier = modifier.testTag("frontier-screen"),
        isScrollableContent = false,
        title = "Action Frontier",
        includeBottomInsets = false,
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            if (maxWidth >= WIDE_FRONTIER_BREAKPOINT) {
                WideFrontierContent(
                    uiState = uiState,
                    onUiEvent = onUiEvent,
                    onPreviewScenario = onPreviewScenario,
                    onOpenActivePlan = onOpenActivePlan,
                )
            } else {
                CompactFrontierContent(
                    uiState = uiState,
                    onUiEvent = onUiEvent,
                    onPreviewScenario = onPreviewScenario,
                    onOpenActivePlan = onOpenActivePlan,
                )
            }
        }
    }
}

@Composable
private fun CompactFrontierContent(
    uiState: FrontierUiState,
    onUiEvent: (FrontierUiEvent) -> Unit,
    onPreviewScenario: (String) -> Unit,
    onOpenActivePlan: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().testTag("frontier-compact-content"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { FrontierIntroduction(uiState = uiState, onUiEvent = onUiEvent) }
        uiState.activePlan?.let { plan ->
            item { ActivePlanPinnedCard(plan = plan, onOpen = onOpenActivePlan) }
        }
        if (uiState.actions.isEmpty()) {
            item { NoFeasibleActionCard() }
        } else {
            items(uiState.actions, key = FrontierActionUiState::id) { action ->
                FrontierCard(
                    action = action,
                    isSelected = false,
                    isSelectable = false,
                    showExplanation = true,
                    onSelect = {},
                    onPreview = { onPreviewScenario(action.id) },
                )
            }
        }
        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
private fun WideFrontierContent(
    uiState: FrontierUiState,
    onUiEvent: (FrontierUiEvent) -> Unit,
    onPreviewScenario: (String) -> Unit,
    onOpenActivePlan: () -> Unit,
) {
    val selectedAction = uiState.actions.firstOrNull { it.id == uiState.selectedActionId }
        ?: uiState.actions.firstOrNull()

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
                item { FrontierIntroduction(uiState = uiState, onUiEvent = onUiEvent) }
                uiState.activePlan?.let { plan ->
                    item { ActivePlanPinnedCard(plan = plan, onOpen = onOpenActivePlan) }
                }
                if (uiState.actions.isEmpty()) {
                    item { NoFeasibleActionCard() }
                } else {
                    items(uiState.actions, key = FrontierActionUiState::id) { action ->
                        FrontierCard(
                            action = action,
                            isSelected = action.id == selectedAction?.id,
                            isSelectable = true,
                            showExplanation = false,
                            onSelect = { onUiEvent(FrontierUiEvent.ActionSelected(action.id)) },
                            onPreview = { onPreviewScenario(action.id) },
                        )
                    }
                }
                item { Spacer(Modifier.height(8.dp)) }
            }

            LazyColumn(
                modifier = Modifier.fillMaxHeight().weight(0.6f).testTag("frontier-detail-pane"),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                if (selectedAction == null) {
                    item { NoSelectionCard() }
                } else {
                    item {
                        FrontierComparisonPanel(
                            action = selectedAction,
                            onOpenFullPreview = { onPreviewScenario(selectedAction.id) },
                        )
                    }
                    item { Spacer(Modifier.height(8.dp)) }
                }
            }
        }
    }
}

@Composable
private fun FrontierIntroduction(
    uiState: FrontierUiState,
    onUiEvent: (FrontierUiEvent) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = when {
                uiState.isSampleMode && uiState.isSampleCatalog -> "SAMPLE PROFILE · SYNTHETIC PACK"
                uiState.isSampleMode -> "SAMPLE PROFILE · ${uiState.catalogDoorCount} SOURCED DOORS"
                uiState.isSampleCatalog -> "YOUR PROFILE · SYNTHETIC PACK"
                else -> "YOUR PROFILE · ${uiState.catalogDoorCount} SOURCED DOORS"
            },
            color = AppTheme.colors.primary,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "Only non-dominated, controllable milestones appear here.",
            color = AppTheme.colors.text.secondary,
            style = AppTheme.typography.bodyLarge,
        )
        ConstraintCard(uiState.horizonDays, onUiEvent)
    }
}

@Composable
private fun ActivePlanPinnedCard(plan: ActivePlanSummaryUiState, onOpen: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("frontier-active-plan"),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.alternative),
        border = BorderStroke(1.dp, PORTA_TEAL),
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("ACTIVE PLAN · ${plan.statusLabel.uppercase()}", color = PORTA_TEAL, fontWeight = FontWeight.Bold)
            Text(plan.title, color = AppTheme.colors.onAlternative, style = AppTheme.typography.h6)
            Button(modifier = Modifier.fillMaxWidth(), onClick = onOpen) { Text("Open plan") }
        }
    }
}

@Composable
private fun ConstraintCard(horizonDays: Int, onUiEvent: (FrontierUiEvent) -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surfaceContainer),
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text("Planning horizon", style = AppTheme.typography.h6)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(listOf(30, 90, 180, 365)) { days ->
                    FilterChip(
                        selected = horizonDays == days,
                        onClick = { onUiEvent(FrontierUiEvent.HorizonChanged(days)) },
                        label = { Text(if (days < 365) "$days days" else "1 year") },
                    )
                }
            }
            Text(
                text = "Up to very-high effort and unknown cost.",
                color = AppTheme.colors.text.secondary,
                style = AppTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun FrontierCard(
    action: FrontierActionUiState,
    isSelected: Boolean,
    isSelectable: Boolean,
    showExplanation: Boolean,
    onSelect: () -> Unit,
    onPreview: () -> Unit,
) {
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
            color = if (isSelected) AppTheme.colors.primary else AppTheme.colors.primary.copy(alpha = 0.55f),
        ),
        shape = RoundedCornerShape(topStart = 38.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 38.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(11.dp),
        ) {
            Text(
                text = if (isSelected) "SELECTED MILESTONE" else "PARETO FRONTIER",
                color = AppTheme.colors.primary,
                fontWeight = FontWeight.Bold,
            )
            Text(action.title, style = AppTheme.typography.h5)
            Text(
                text = buildString {
                    append("${action.improvedCount} hard gaps resolved")
                    if (action.newlyOpenCount > 0) append(" · ${action.newlyOpenCount} open")
                    if (action.savedDoorCount > 0) append(" · ${action.savedDoorCount} saved")
                },
                color = PORTA_TEAL,
                style = AppTheme.typography.h6,
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricPill(action.durationLabel, Modifier.weight(1f))
                MetricPill(action.effortLabel, Modifier.weight(1f))
                MetricPill(action.costLabel, Modifier.weight(1f))
            }
            if (showExplanation) {
                Text(
                    text = action.explanation,
                    color = AppTheme.colors.text.secondary,
                    style = AppTheme.typography.bodyMedium,
                )
                Text(
                    text = action.doorTitles.joinToString(prefix = "Moves forward: ", separator = " · "),
                    style = AppTheme.typography.bodyMedium,
                )
            }
            Button(modifier = Modifier.fillMaxWidth(), onClick = onPreview) {
                Text(if (isSelectable) "Open full preview" else "Preview what changes")
            }
        }
    }
}

@Composable
private fun FrontierComparisonPanel(
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
                Text("ACTUAL VS WHAT-IF", color = AppTheme.colors.primary, fontWeight = FontWeight.ExtraBold)
                Text(action.title, style = AppTheme.typography.h4, color = AppTheme.colors.text.primary)
                Text(
                    text = "A read-only comparison. Nothing here changes your saved profile.",
                    style = AppTheme.typography.bodyMedium,
                    color = AppTheme.colors.text.secondary,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                ValueComparisonCard(
                    eyebrow = "ACTUAL PROFILE",
                    value = action.currentLabel,
                    modifier = Modifier.weight(1f),
                )
                ValueComparisonCard(
                    eyebrow = "WHAT-IF PREVIEW",
                    value = action.targetLabel,
                    modifier = Modifier.weight(1f),
                    emphasized = true,
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Why it ranks", style = AppTheme.typography.h6, color = AppTheme.colors.text.primary)
                Text(
                    text = action.explanation,
                    style = AppTheme.typography.bodyMedium,
                    color = AppTheme.colors.text.secondary,
                )
            }

            HorizontalDivider(color = AppTheme.colors.outline.copy(alpha = 0.35f))
            Text(
                text = "Affected doors · ${action.doorComparisons.size}",
                style = AppTheme.typography.h6,
                color = AppTheme.colors.text.primary,
            )
            DoorComparisonHeader()
            action.doorComparisons.forEach { door ->
                DoorComparisonRow(door)
            }
            if (action.doorComparisons.isEmpty()) {
                Text(
                    text = "Open the full preview to inspect this milestone.",
                    style = AppTheme.typography.bodyMedium,
                    color = AppTheme.colors.text.secondary,
                )
            }
            Button(
                modifier = Modifier.fillMaxWidth().testTag("frontier-open-preview-${action.id}"),
                onClick = onOpenFullPreview,
            ) {
                Text("Open full preview")
            }
        }
    }
}

@Composable
private fun ValueComparisonCard(
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
private fun DoorComparisonHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ComparisonHeaderLabel("Door", Modifier.weight(1.4f))
        ComparisonHeaderLabel("Actual", Modifier.weight(0.8f))
        ComparisonHeaderLabel("Preview", Modifier.weight(0.8f))
    }
}

@Composable
private fun ComparisonHeaderLabel(text: String, modifier: Modifier) {
    Text(
        text = text,
        modifier = modifier,
        style = AppTheme.typography.bodySmall,
        color = AppTheme.colors.text.secondary,
        fontWeight = FontWeight.Bold,
    )
}

@Composable
private fun DoorComparisonRow(door: FrontierDoorComparisonUiState) {
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
                label = door.actualStateLabel,
                modifier = Modifier.weight(0.8f),
                highlighted = false,
            )
            StateLabel(
                label = door.previewStateLabel,
                modifier = Modifier.weight(0.8f),
                highlighted = door.isNewlyOpen,
            )
        }
        if (door.isSaved || door.requiresDeadlineCheck) {
            Text(
                text = buildList {
                    if (door.isSaved) add("Saved door")
                    if (door.requiresDeadlineCheck) add("Deadline needs verification")
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
private fun StateLabel(
    label: String,
    modifier: Modifier = Modifier,
    highlighted: Boolean,
) {
    Surface(
        modifier = modifier,
        color = if (highlighted) PORTA_TEAL.copy(alpha = 0.16f) else AppTheme.colors.background,
        shape = RoundedCornerShape(10.dp),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 7.dp),
            style = AppTheme.typography.bodySmall,
            color = if (highlighted) PORTA_TEAL else AppTheme.colors.text.secondary,
            fontWeight = if (highlighted) FontWeight.Bold else FontWeight.Medium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
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
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            color = AppTheme.colors.onAlternative,
            style = AppTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun NoFeasibleActionCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surfaceContainer),
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("No feasible milestone in this horizon", style = AppTheme.typography.h6)
            Text(
                text = "Try a longer horizon. Porta will not pretend a 45-day milestone fits into 30 days.",
                color = AppTheme.colors.text.secondary,
                style = AppTheme.typography.bodyLarge,
            )
        }
    }
}

@Composable
private fun NoSelectionCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surfaceContainer),
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("No milestone to compare", style = AppTheme.typography.h5)
            Text(
                text = "Adjust the planning horizon to find a feasible next step.",
                color = AppTheme.colors.text.secondary,
                style = AppTheme.typography.bodyLarge,
            )
        }
    }
}

private val WIDE_FRONTIER_BREAKPOINT = 900.dp
private val PORTA_TEAL = Color(0xFF2DD4BF)

@Preview
@Composable
private fun FrontierScreenPreview() {
    AppTheme(isDarkMode = true) {
        FrontierScreen(
            uiState = previewState,
            onUiEvent = {},
            onPreviewScenario = {},
            onOpenActivePlan = {},
        )
    }
}

@Preview(device = "spec:width=1280dp,height=800dp,dpi=160")
@Composable
private fun FrontierDesktopPreview() {
    AppTheme {
        FrontierScreen(
            uiState = previewState.copy(selectedActionId = "raise-ielts"),
            onUiEvent = {},
            onPreviewScenario = {},
            onOpenActivePlan = {},
        )
    }
}

private val previewState = FrontierUiState(
    isSampleMode = false,
    isSampleCatalog = false,
    catalogDoorCount = 15,
    actions = listOf(
        FrontierActionUiState(
            id = "raise-ielts",
            title = "Raise IELTS from 6.5 to 7.0",
            currentLabel = "6.5 band",
            targetLabel = "7.0 band",
            newlyOpenCount = 2,
            improvedCount = 4,
            savedDoorCount = 1,
            timingUnverifiedCount = 1,
            durationLabel = "45 days",
            effortLabel = "Medium",
            costLabel = "Low",
            explanation = "Resolves an encoded hard-rule gap in four opportunities. Two become open.",
            doorTitles = listOf("Aurora", "Forge", "Horizon", "Spark"),
            doorComparisons = listOf(
                FrontierDoorComparisonUiState(
                    id = "aurora",
                    title = "Aurora Scholars",
                    actualStateLabel = "Has gaps",
                    previewStateLabel = "Open",
                    isNewlyOpen = true,
                    isSaved = true,
                    requiresDeadlineCheck = false,
                ),
                FrontierDoorComparisonUiState(
                    id = "erasmus",
                    title = "Erasmus Mundus Joint Masters",
                    actualStateLabel = "Has gaps",
                    previewStateLabel = "Needs info",
                    isNewlyOpen = false,
                    isSaved = false,
                    requiresDeadlineCheck = true,
                ),
            ),
        ),
    ),
)
