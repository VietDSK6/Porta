package io.github.vietdsk6.porta.presentation.screens.frontier

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.vietdsk6.porta.designsystem.components.ScreenWithToolbar
import io.github.vietdsk6.porta.designsystem.theme.AppTheme
import io.github.vietdsk6.porta.domain.model.CostBand
import io.github.vietdsk6.porta.domain.model.EffortLevel

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
        title = "Next moves",
        includeBottomInsets = false,
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val useWideLayout = maxWidth >= WIDE_FRONTIER_BREAKPOINT && LocalDensity.current.fontScale < 1.5f
            if (useWideLayout) {
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
    val recommendedAction = uiState.actions.firstOrNull()
    val alternativeFamilies = uiState.alternativeFamilies()
    LazyColumn(
        modifier = Modifier.fillMaxSize().testTag("frontier-compact-content"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { FrontierIntroduction(uiState = uiState) }
        recommendedAction?.let { action ->
            item {
                FrontierCard(
                    action = action,
                    eyebrow = "RECOMMENDED NEXT MOVE",
                    isRecommended = true,
                    isSelected = false,
                    isSelectable = false,
                    showExplanation = true,
                    onSelect = {},
                    onPreview = { onPreviewScenario(action.id) },
                )
            }
        }
        uiState.activePlan?.let { plan ->
            item { ActivePlanPinnedCard(plan = plan, onOpen = onOpenActivePlan) }
        }
        item {
            FrontierControls(
                uiState = uiState,
                onUiEvent = onUiEvent,
                collapsible = true,
            )
        }
        if (uiState.actions.isEmpty()) {
            item { NoFeasibleActionCard(uiState.exclusions) }
        } else {
            if (alternativeFamilies.isNotEmpty()) {
                item {
                    SectionIntroduction(
                        title = "Alternatives",
                        description = "Grouped by the kind of change. Each programme check remains separate.",
                    )
                }
            }
            items(alternativeFamilies, key = FrontierActionFamilyUiState::id) { family ->
                AlternativeFamilySection(
                    family = family,
                    selectedActionId = null,
                    onSelect = {},
                    onPreview = onPreviewScenario,
                )
            }
            if (uiState.hasMeaningfulTradeOffs) {
                item {
                    FrontierTradeOffsCard(
                        uiState = uiState,
                        onUiEvent = onUiEvent,
                    )
                }
            }
        }
        if (uiState.exclusions.isNotEmpty()) {
            item { ExcludedActionsCard(uiState.exclusions) }
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
                recommendedAction?.let { action ->
                    item {
                        FrontierCard(
                            action = action,
                            eyebrow = "RECOMMENDED NEXT MOVE",
                            isRecommended = true,
                            isSelected = action.id == selectedAction?.id,
                            isSelectable = true,
                            showExplanation = true,
                            onSelect = { onUiEvent(FrontierUiEvent.ActionSelected(action.id)) },
                            onPreview = { onPreviewScenario(action.id) },
                        )
                    }
                }
                uiState.activePlan?.let { plan ->
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
                    item { NoSelectionCard() }
                } else {
                    item {
                        FrontierComparisonPanel(
                            action = selectedAction,
                            onOpenFullPreview = { onPreviewScenario(selectedAction.id) },
                        )
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
private fun FrontierIntroduction(
    uiState: FrontierUiState,
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
            text = "Start with one measurable step, then preview which checked requirements it could change.",
            color = AppTheme.colors.text.secondary,
            style = AppTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun SectionIntroduction(title: String, description: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = AppTheme.typography.h5)
        Text(
            text = description,
            color = AppTheme.colors.text.secondary,
            style = AppTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun AlternativeFamilySection(
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
                text = "${family.kind.label()} · ${family.title}",
                color = AppTheme.colors.primary,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = if (family.actions.size == 1) {
                    family.kind.description()
                } else {
                    "${family.actions.size} programme-specific checks share this planning pattern."
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
private fun AlternativeActionRow(
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
            Text(action.scopeLabel, color = AppTheme.colors.text.secondary, style = AppTheme.typography.bodySmall)
            Text(action.title, style = AppTheme.typography.h6)
            Text(
                text = "${action.improvedCount} " +
                    if (action.improvedCount == 1) "door affected" else "doors affected",
                color = PORTA_TEAL,
                fontWeight = FontWeight.SemiBold,
                style = AppTheme.typography.bodyMedium,
            )
            Text(
                text = "${action.durationLabel} · ${action.effortLabel} effort · ${action.costLabel} cost",
                color = AppTheme.colors.text.secondary,
                style = AppTheme.typography.bodySmall,
            )
            Text(
                text = "${action.estimateSourceLabel} · ${action.impactBasisLabel}",
                color = AppTheme.colors.text.secondary,
                style = AppTheme.typography.bodySmall,
            )
            TextButton(
                modifier = Modifier.align(Alignment.End),
                onClick = onPreview,
            ) {
                Text("Preview changes")
            }
        }
    }
}

@Composable
private fun FrontierTradeOffsCard(
    uiState: FrontierUiState,
    onUiEvent: (FrontierUiEvent) -> Unit,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("frontier-trade-offs-toggle")
                .semantics {
                    contentDescription = "Action impact map comparison, " +
                        if (expanded) "expanded" else "collapsed"
                }
                .clickable(
                    role = Role.Button,
                    onClickLabel = if (expanded) "Hide trade-offs" else "Compare trade-offs",
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
                    Text("Compare trade-offs", style = AppTheme.typography.h6)
                    Text(
                        "Action Frontier · time, impact and cost",
                        color = AppTheme.colors.text.secondary,
                        style = AppTheme.typography.bodySmall,
                    )
                }
                Text(
                    text = if (expanded) "Hide" else "Compare",
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
private fun FrontierCard(
    action: FrontierActionUiState,
    eyebrow: String,
    isRecommended: Boolean,
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
                text = "$eyebrow · ${action.kind.label()}",
                color = AppTheme.colors.primary,
                fontWeight = FontWeight.Bold,
            )
            Text(action.title, style = AppTheme.typography.h5)
            Text(
                text = action.scopeLabel,
                color = AppTheme.colors.text.secondary,
                style = AppTheme.typography.bodySmall,
            )
            Text(
                text = buildString {
                    append("${action.improvedCount} ${if (action.improvedCount == 1) "door" else "doors"} affected")
                    if (action.newlyOpenCount > 0) {
                        append(" · ${action.newlyOpenCount} would pass current checks")
                    }
                    if (action.savedDoorCount > 0) append(" · ${action.savedDoorCount} saved")
                },
                color = PORTA_TEAL,
                style = AppTheme.typography.h6,
            )
            AdaptiveMetricPills(action)
            if (showExplanation) {
                Text(
                    text = action.explanation,
                    color = AppTheme.colors.text.secondary,
                    style = AppTheme.typography.bodyMedium,
                )
                Text(
                    text = action.doorTitles.joinToString(prefix = "Affected doors: ", separator = " · "),
                    style = AppTheme.typography.bodyMedium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = action.estimateSourceLabel,
                    color = AppTheme.colors.text.secondary,
                    style = AppTheme.typography.bodySmall,
                )
                Text(
                    text = action.impactBasisLabel,
                    color = AppTheme.colors.text.secondary,
                    style = AppTheme.typography.bodySmall,
                )
            }
            Button(modifier = Modifier.fillMaxWidth(), onClick = onPreview) {
                Text("Preview changes")
            }
        }
    }
}

@Composable
private fun AdaptiveMetricPills(action: FrontierActionUiState) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val stackMetrics = maxWidth < 320.dp || LocalDensity.current.fontScale >= 1.5f
        if (stackMetrics) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricPill(action.durationLabel, Modifier.fillMaxWidth())
                MetricPill(action.effortLabel, Modifier.fillMaxWidth())
                MetricPill(action.costLabel, Modifier.fillMaxWidth())
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricPill(action.durationLabel, Modifier.weight(1f))
                MetricPill(action.effortLabel, Modifier.weight(1f))
                MetricPill(action.costLabel, Modifier.weight(1f))
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
                Text("SIMULATION PREVIEW", color = AppTheme.colors.primary, fontWeight = FontWeight.ExtraBold)
                Text(action.title, style = AppTheme.typography.h4, color = AppTheme.colors.text.primary)
                Text(
                    text = "Simulation — your saved profile is unchanged.",
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
                    text = "Preview this step to inspect its checked requirement.",
                    style = AppTheme.typography.bodyMedium,
                    color = AppTheme.colors.text.secondary,
                )
            }
            Button(
                modifier = Modifier.fillMaxWidth().testTag("frontier-open-preview-${action.id}"),
                onClick = onOpenFullPreview,
            ) {
                Text("Preview changes")
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
        ComparisonHeaderLabel("Simulated", Modifier.weight(0.8f))
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
                    if (door.requiresDeadlineCheck) add("Deadline needs confirmation")
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
private fun NoFeasibleActionCard(exclusions: List<FrontierExcludedActionUiState>) {
    Card(
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surfaceContainer),
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = if (exclusions.isEmpty()) "No reliable next move yet" else "Nothing fits these limits",
                style = AppTheme.typography.h6,
            )
            Text(
                text = if (exclusions.isEmpty()) {
                    "Porta cannot support a recommendation from the encoded checks yet. Add relevant profile facts or " +
                        "review a programme source first."
                } else {
                    "Porta did not silently hide the candidates. Review the reasons below or widen one limit."
                },
                color = AppTheme.colors.text.secondary,
                style = AppTheme.typography.bodyLarge,
            )
        }
    }
}

private fun FrontierUiState.alternativeFamilies(): List<FrontierActionFamilyUiState> {
    val recommendedId = actions.firstOrNull()?.id
    return actionFamilies.mapNotNull { family ->
        family.copy(actions = family.actions.filterNot { it.id == recommendedId })
            .takeIf { it.actions.isNotEmpty() }
    }
}

private fun FrontierMoveKind.label(): String = when (this) {
    FrontierMoveKind.VERIFY -> "VERIFY"
    FrontierMoveKind.IMPROVE -> "IMPROVE"
    FrontierMoveKind.PREPARE -> "PREPARE"
}

private fun FrontierMoveKind.description(): String = when (this) {
    FrontierMoveKind.VERIFY -> "Confirm a programme-specific check without changing your saved profile."
    FrontierMoveKind.IMPROVE -> "Change a controllable profile fact and inspect the encoded rule impact."
    FrontierMoveKind.PREPARE -> "Plan a time-dependent milestone before relying on its future impact."
}

@Composable
private fun ExcludedActionsCard(exclusions: List<FrontierExcludedActionUiState>) {
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
                Text("Outside your limits · ${exclusions.size}", style = AppTheme.typography.h6)
                Text(
                    text = "These are candidates, not recommendations under your current limits.",
                    color = AppTheme.colors.text.secondary,
                    style = AppTheme.typography.bodySmall,
                )
            }
            exclusions.forEach { exclusion ->
                Column(
                    modifier = Modifier.fillMaxWidth().testTag("frontier-exclusion-${exclusion.id}"),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(exclusion.title, fontWeight = FontWeight.SemiBold)
                    exclusion.reasons.forEach { reason ->
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
                text = "Adjust time, effort or cost to find a feasible next step.",
                color = AppTheme.colors.text.secondary,
                style = AppTheme.typography.bodyLarge,
            )
        }
    }
}

private val WIDE_FRONTIER_BREAKPOINT = 900.dp
private val PORTA_TEAL = Color(0xFF167D73)

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
    isSampleMode = true,
    isSampleCatalog = true,
    catalogDoorCount = 5,
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
            durationDays = 45,
            effort = EffortLevel.MEDIUM,
            cost = CostBand.LOW,
            durationLabel = "45 days",
            effortLabel = "Medium",
            costLabel = "Low",
            explanation = "Addresses a published requirement in four opportunities. Two would pass their current checks.",
            doorTitles = listOf("Aurora", "Forge", "Horizon", "Spark"),
            doorComparisons = listOf(
                FrontierDoorComparisonUiState(
                    id = "aurora",
                    title = "Aurora Scholars",
                    actualStateLabel = "Needs action",
                    previewStateLabel = "Assessed rules passed",
                    isNewlyOpen = true,
                    isSaved = true,
                    requiresDeadlineCheck = false,
                ),
                FrontierDoorComparisonUiState(
                    id = "erasmus",
                    title = "Erasmus Mundus Joint Masters",
                    actualStateLabel = "Needs action",
                    previewStateLabel = "Needs information",
                    isNewlyOpen = false,
                    isSaved = false,
                    requiresDeadlineCheck = true,
                ),
            ),
        ),
        FrontierActionUiState(
            id = "verify-enrollment",
            title = "Verify current enrollment",
            currentLabel = "Self-reported",
            targetLabel = "Evidence attached",
            newlyOpenCount = 1,
            improvedCount = 2,
            savedDoorCount = 2,
            durationDays = 7,
            effort = EffortLevel.LOW,
            cost = CostBand.FREE,
            durationLabel = "7 days",
            effortLabel = "Low",
            costLabel = "Free",
            explanation = "Addresses an enrollment-evidence check in two saved opportunities.",
            doorTitles = listOf("Kotlin Student Coding Competition", "Global Student Challenge"),
        ),
        FrontierActionUiState(
            id = "community-project",
            title = "Complete an evidenced community project",
            currentLabel = "Not set",
            targetLabel = "Completed",
            newlyOpenCount = 1,
            improvedCount = 3,
            savedDoorCount = 0,
            durationDays = 90,
            effort = EffortLevel.HIGH,
            cost = CostBand.MEDIUM,
            durationLabel = "90 days",
            effortLabel = "High",
            costLabel = "Medium",
            explanation = "Addresses a published project requirement in three opportunities.",
            doorTitles = listOf("Impact Fellows", "Civic Builders", "Student Changemakers"),
        ),
    ),
    exclusions = listOf(
        FrontierExcludedActionUiState(
            id = "internship-year",
            title = "Build 12 months of experience",
            reasons = listOf("Needs more time than your horizon"),
        ),
    ),
)
