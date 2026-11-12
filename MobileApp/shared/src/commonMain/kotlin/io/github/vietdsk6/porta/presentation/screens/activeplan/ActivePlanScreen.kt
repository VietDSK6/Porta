package io.github.vietdsk6.porta.presentation.screens.activeplan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.vietdsk6.porta.designsystem.components.ScreenWithToolbar
import io.github.vietdsk6.porta.designsystem.generated.resources.UiRes
import io.github.vietdsk6.porta.designsystem.generated.resources.ic_back
import io.github.vietdsk6.porta.designsystem.theme.AppTheme
import io.github.vietdsk6.porta.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun ActivePlanScreen(
    viewModel: ActivePlanViewModel,
    onNavigateBack: () -> Unit,
    onBrowseActions: () -> Unit,
    onPreviewImpact: (String) -> Unit,
    onUpdateProfile: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ActivePlanScreen(
        modifier = modifier.fillMaxSize(),
        uiState = uiState,
        onUiEvent = viewModel::onUiEvent,
        onNavigateBack = onNavigateBack,
        onBrowseActions = onBrowseActions,
        onPreviewImpact = onPreviewImpact,
        onUpdateProfile = onUpdateProfile,
    )
}

@Composable
fun ActivePlanScreen(
    uiState: ActivePlanUiState,
    onUiEvent: (ActivePlanUiEvent) -> Unit,
    onNavigateBack: () -> Unit,
    onBrowseActions: () -> Unit,
    onPreviewImpact: (String) -> Unit,
    onUpdateProfile: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ScreenWithToolbar(
        modifier = modifier.testTag("active-plan-screen"),
        isScrollableContent = false,
        title = stringResource(Res.string.active_title),
        includeBottomInsets = true,
        navigationIcon = UiRes.drawable.ic_back,
        onNavigationIconClick = onNavigateBack,
    ) {
        when {
            uiState.isLoading -> LoadingState()

            uiState.plan == null -> EmptyPlanState(onBrowseActions)

            else -> ActivePlanContent(
                uiState = uiState,
                onUiEvent = onUiEvent,
                onPreviewImpact = onPreviewImpact,
                onUpdateProfile = onUpdateProfile,
            )
        }
    }
}

@Composable
internal fun LoadingState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator()
        Text(
            stringResource(Res.string.active_loading),
            modifier = Modifier.padding(top = 12.dp),
            color = AppTheme.colors.text.secondary,
        )
    }
}

@Composable
internal fun EmptyPlanState(onBrowseActions: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surfaceContainer),
            shape = RoundedCornerShape(28.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(stringResource(Res.string.active_empty_title), style = AppTheme.typography.h5)
                Text(
                    stringResource(Res.string.active_empty_help),
                    color = AppTheme.colors.text.secondary,
                    style = AppTheme.typography.bodyLarge,
                )
                Button(
                    modifier = Modifier.fillMaxWidth().testTag("browse-actions-button"),
                    onClick = onBrowseActions,
                ) {
                    Text(stringResource(Res.string.active_browse))
                }
            }
        }
    }
}

@Composable
internal fun ActivePlanContent(
    uiState: ActivePlanUiState,
    onUiEvent: (ActivePlanUiEvent) -> Unit,
    onPreviewImpact: (String) -> Unit,
    onUpdateProfile: () -> Unit,
) {
    val plan = checkNotNull(uiState.plan)
    val localizedError = uiState.localizedError()
    LazyColumn(
        modifier = Modifier.fillMaxSize().testTag("active-plan-list"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    if (uiState.isSampleCatalog) {
                        stringResource(Res.string.active_synthetic_badge)
                    } else {
                        stringResource(Res.string.active_sourced_badge)
                    },
                    color = AppTheme.colors.primary,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    stringResource(Res.string.active_local_notice),
                    color = AppTheme.colors.text.secondary,
                    style = AppTheme.typography.bodyLarge,
                )
            }
        }
        if (uiState.routeSteps.isNotEmpty()) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().testTag("active-route-steps"),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(stringResource(Res.string.active_route_milestones), style = AppTheme.typography.h6)
                    uiState.routeSteps.forEach { step ->
                        val selected = step.planId == uiState.selectedPlanId
                        if (selected) {
                            Button(
                                modifier = Modifier.fillMaxWidth().testTag("route-step-${step.sequence}"),
                                onClick = { onUiEvent(ActivePlanUiEvent.SelectStep(step.planId)) },
                            ) {
                                Text(stringResource(Res.string.active_numbered_step, step.sequence + 1, step.title))
                            }
                        } else {
                            OutlinedButton(
                                modifier = Modifier.fillMaxWidth().testTag("route-step-${step.sequence}"),
                                onClick = { onUiEvent(ActivePlanUiEvent.SelectStep(step.planId)) },
                            ) {
                                Text(stringResource(Res.string.active_numbered_step, step.sequence + 1, step.title))
                            }
                        }
                    }
                }
            }
        }
        item { GoalCard(plan) }
        item { PlanTimeline(plan) }
        item { EvidenceAndImpactCard(plan) }
        item { RiskCard(plan.localizedRisk()) }
        if (plan.status == ActivePlanStatusUi.COMPLETED) {
            item { CompletionBoundaryCard() }
        }
        localizedError?.let { error ->
            item { Text(error, color = AppTheme.colors.status.error, style = AppTheme.typography.bodyMedium) }
        }
        item {
            PlanActions(
                plan = plan,
                isWorking = uiState.isWorking,
                onUiEvent = onUiEvent,
                onPreviewImpact = onPreviewImpact,
                onUpdateProfile = onUpdateProfile,
            )
        }
        item { Spacer(Modifier.height(8.dp)) }
    }
}
