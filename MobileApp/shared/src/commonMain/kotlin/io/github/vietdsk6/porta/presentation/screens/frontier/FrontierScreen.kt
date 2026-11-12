package io.github.vietdsk6.porta.presentation.screens.frontier

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.vietdsk6.porta.designsystem.components.ScreenWithToolbar
import io.github.vietdsk6.porta.designsystem.theme.AppTheme
import io.github.vietdsk6.porta.generated.resources.Res
import io.github.vietdsk6.porta.generated.resources.frontier_alternatives
import io.github.vietdsk6.porta.generated.resources.frontier_alternatives_help
import io.github.vietdsk6.porta.generated.resources.frontier_intro
import io.github.vietdsk6.porta.generated.resources.frontier_intro_demo_sourced
import io.github.vietdsk6.porta.generated.resources.frontier_intro_demo_synthetic
import io.github.vietdsk6.porta.generated.resources.frontier_intro_sample_sourced
import io.github.vietdsk6.porta.generated.resources.frontier_intro_sample_synthetic
import io.github.vietdsk6.porta.generated.resources.frontier_intro_your_sourced
import io.github.vietdsk6.porta.generated.resources.frontier_intro_your_synthetic
import io.github.vietdsk6.porta.generated.resources.frontier_title
import io.github.vietdsk6.porta.presentation.theme.PortaBreakpoints
import org.jetbrains.compose.resources.stringResource

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
        title = stringResource(Res.string.frontier_title),
        includeBottomInsets = false,
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val hasDesktopWidth = maxWidth >= PortaBreakpoints.workbench
            val useWideLayout = hasDesktopWidth &&
                LocalDensity.current.fontScale < PortaBreakpoints.maxWideFontScale
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
                    desktopCompareEnabled = hasDesktopWidth,
                )
            }
        }
    }
}

@Composable
internal fun CompactFrontierContent(
    uiState: FrontierUiState,
    onUiEvent: (FrontierUiEvent) -> Unit,
    onPreviewScenario: (String) -> Unit,
    onOpenActivePlan: () -> Unit,
    desktopCompareEnabled: Boolean = false,
) {
    val recommendedAction = uiState.actions.firstOrNull()
    val alternativeFamilies = uiState.alternativeFamilies()
    LazyColumn(
        modifier = Modifier.fillMaxSize().testTag("frontier-compact-content"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { FrontierIntroduction(uiState = uiState) }
        if (desktopCompareEnabled && uiState.actions.size >= 2) {
            item {
                DesktopCompareSelector(
                    actions = uiState.actions,
                    comparedActionIds = uiState.comparedActionIds,
                    onToggle = { onUiEvent(FrontierUiEvent.ComparisonToggled(it)) },
                    onClear = { onUiEvent(FrontierUiEvent.ComparisonCleared) },
                )
            }
            if (uiState.comparedActions.size >= 2) {
                item { DesktopScenarioCompareLab(actions = uiState.comparedActions) }
            }
        }
        uiState.activePlan?.takeIf { it.needsPriorityPlacement }?.let { plan ->
            item { ActivePlanPinnedCard(plan = plan, onOpen = onOpenActivePlan) }
        }
        recommendedAction?.let { action ->
            item {
                FrontierCard(
                    action = action,
                    eyebrow = action.kind.suggestionLabel(),
                    isRecommended = true,
                    isSelected = false,
                    isSelectable = false,
                    showExplanation = true,
                    onSelect = {},
                    onPreview = { onPreviewScenario(action.id) },
                )
            }
            if (action.doorComparisons.isNotEmpty()) {
                item { RecommendedRippleDisclosure(action) }
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
                collapsible = true,
            )
        }
        if (uiState.actions.isEmpty()) {
            item { NoFeasibleActionCard(uiState.exclusions) }
        } else {
            if (alternativeFamilies.isNotEmpty()) {
                item {
                    SectionIntroduction(
                        title = stringResource(Res.string.frontier_alternatives),
                        description = stringResource(Res.string.frontier_alternatives_help),
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
internal fun FrontierIntroduction(
    uiState: FrontierUiState,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = when {
                uiState.isDemoProfile && uiState.isSampleCatalog -> stringResource(Res.string.frontier_intro_demo_synthetic)
                uiState.isDemoProfile -> stringResource(Res.string.frontier_intro_demo_sourced, uiState.catalogDoorCount)
                uiState.isSampleMode && uiState.isSampleCatalog -> stringResource(Res.string.frontier_intro_sample_synthetic)
                uiState.isSampleMode -> stringResource(Res.string.frontier_intro_sample_sourced, uiState.catalogDoorCount)
                uiState.isSampleCatalog -> stringResource(Res.string.frontier_intro_your_synthetic)
                else -> stringResource(Res.string.frontier_intro_your_sourced, uiState.catalogDoorCount)
            },
            color = AppTheme.colors.primary,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = stringResource(Res.string.frontier_intro),
            color = AppTheme.colors.text.secondary,
            style = AppTheme.typography.bodyLarge,
        )
    }
}

@Composable
internal fun SectionIntroduction(title: String, description: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = AppTheme.typography.h5)
        Text(
            text = description,
            color = AppTheme.colors.text.secondary,
            style = AppTheme.typography.bodyMedium,
        )
    }
}
