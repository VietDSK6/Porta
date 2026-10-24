package io.github.vietdsk6.porta.presentation.screens.scenariopreview

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.vietdsk6.porta.designsystem.components.ScreenWithToolbar
import io.github.vietdsk6.porta.designsystem.generated.resources.UiRes
import io.github.vietdsk6.porta.designsystem.generated.resources.ic_back
import io.github.vietdsk6.porta.designsystem.theme.AppTheme

@Composable
fun ScenarioPreviewScreen(
    viewModel: ScenarioPreviewViewModel,
    onNavigateBack: () -> Unit,
    onPlanSaved: () -> Unit,
    onBuildProfile: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            viewModel.onUiEvent(ScenarioPreviewUiEvent.SaveHandled)
            onPlanSaved()
        }
    }
    ScenarioPreviewScreen(
        uiState = uiState,
        onUiEvent = viewModel::onUiEvent,
        onNavigateBack = onNavigateBack,
        onBuildProfile = onBuildProfile,
        modifier = modifier.fillMaxSize(),
    )
}

@Composable
fun ScenarioPreviewScreen(
    uiState: ScenarioPreviewUiState,
    onUiEvent: (ScenarioPreviewUiEvent) -> Unit,
    onNavigateBack: () -> Unit,
    onBuildProfile: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ScreenWithToolbar(
        modifier = modifier.testTag("scenario-preview-screen"),
        isScrollableContent = false,
        title = "What if?",
        includeBottomInsets = true,
        navigationIcon = UiRes.drawable.ic_back,
        onNavigationIconClick = onNavigateBack,
    ) {
        if (uiState.error != null) {
            ErrorState(uiState.error, onNavigateBack)
            return@ScreenWithToolbar
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    SimulationBanner()
                    BeforeAfterCard(uiState)
                }
            }
            item {
                val affectedCount = uiState.openedDoors.size + uiState.progressedDoors.size
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Changed doors", style = AppTheme.typography.h5)
                    Text(
                        text = if (affectedCount == 1) {
                            "1 door is affected by this simulation."
                        } else {
                            "$affectedCount doors are affected by this simulation."
                        },
                        color = AppTheme.colors.text.secondary,
                        style = AppTheme.typography.bodyMedium,
                    )
                }
            }
            items(uiState.openedDoors, key = ScenarioDoorUiState::id) { door ->
                ScenarioDoorCard(door = door)
            }
            items(uiState.progressedDoors, key = ScenarioDoorUiState::id) { door ->
                ScenarioDoorCard(door = door)
            }
            if (uiState.openedDoors.isEmpty() && uiState.progressedDoors.isEmpty()) {
                item { NoChangedDoorsCard() }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isSaving,
                        onClick = {
                            if (uiState.isSampleProfile) {
                                onBuildProfile()
                            } else {
                                onUiEvent(ScenarioPreviewUiEvent.SavePlan)
                            }
                        },
                    ) {
                        Text(
                            when {
                                uiState.isSampleProfile -> "Build my profile to save"
                                uiState.isSaving -> "Saving locally…"
                                else -> "Add milestone to plan"
                            },
                        )
                    }
                    uiState.saveError?.let { message ->
                        Text(message, color = AppTheme.colors.status.error, style = AppTheme.typography.bodyMedium)
                    }
                    OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = onNavigateBack) {
                        Text("Discard scenario")
                    }
                }
            }
        }
    }
}

@Composable
private fun SimulationBanner() {
    Surface(
        modifier = Modifier.fillMaxWidth().testTag("scenario-simulation-banner"),
        color = AppTheme.colors.alternative,
        shape = RoundedCornerShape(14.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = "Simulation — your saved profile is unchanged.",
                color = AppTheme.colors.onAlternative,
                fontWeight = FontWeight.Bold,
                style = AppTheme.typography.bodyMedium,
            )
            Text(
                text = "Check every conclusion against the linked official source.",
                color = AppTheme.colors.onAlternative.copy(alpha = 0.74f),
                style = AppTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun BeforeAfterCard(uiState: ScenarioPreviewUiState) {
    Card(
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surfaceContainer),
        border = BorderStroke(1.dp, AppTheme.colors.outline.copy(alpha = 0.35f)),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(uiState.title, style = AppTheme.typography.h5)
            uiState.scopeNotice?.let { notice ->
                Text(
                    text = notice,
                    color = AppTheme.colors.primary,
                    style = AppTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ValueCell("Actual", uiState.beforeLabel, Modifier.weight(1f))
                ValueCell("Scenario", uiState.afterLabel, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ValueCell(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.alternative),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(Modifier.fillMaxWidth().padding(14.dp)) {
            Text(label, color = AppTheme.colors.text.secondary, style = AppTheme.typography.bodySmall)
            Text(value, color = AppTheme.colors.onAlternative, style = AppTheme.typography.h6)
        }
    }
}

@Composable
private fun ScenarioDoorCard(door: ScenarioDoorUiState) {
    var visible by remember(door.id) { mutableStateOf(false) }
    LaunchedEffect(door.id) { visible = true }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(DOOR_TRANSITION_DURATION_MILLIS)) +
            slideInVertically(tween(DOOR_TRANSITION_DURATION_MILLIS)) { height -> height / 12 },
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surfaceContainer),
            border = BorderStroke(1.dp, AppTheme.colors.outline.copy(alpha = 0.35f)),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = door.title,
                    modifier = Modifier.fillMaxWidth().testTag("scenario-door-title-${door.id}"),
                    style = AppTheme.typography.h6,
                )
                ScenarioStateTransition(door)
                Text(door.stateLabel, color = PORTA_TEAL, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ScenarioStateTransition(door: ScenarioDoorUiState) {
    val actual = door.actualStateLabel.ifBlank { "Current state" }
    val simulated = door.simulatedStateLabel.ifBlank { door.stateLabel }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        StateCell(label = "Actual", value = actual, modifier = Modifier.weight(1f))
        Text(
            text = "→",
            modifier = Modifier.padding(top = 24.dp),
            color = AppTheme.colors.text.secondary,
            fontWeight = FontWeight.Bold,
        )
        StateCell(label = "Simulated", value = simulated, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun StateCell(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(label, color = AppTheme.colors.text.secondary, style = AppTheme.typography.bodySmall)
        Text(value, style = AppTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun NoChangedDoorsCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = AppTheme.colors.surfaceContainer,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, AppTheme.colors.outline.copy(alpha = 0.35f)),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text("No door status changes", fontWeight = FontWeight.SemiBold)
            Text(
                text = "This step may still help your profile, but it does not change any published-check conclusion yet.",
                color = AppTheme.colors.text.secondary,
                style = AppTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun ErrorState(message: String, onNavigateBack: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Scenario unavailable", style = AppTheme.typography.h5)
        Text(message, color = AppTheme.colors.text.secondary)
        OutlinedButton(onClick = onNavigateBack) { Text("Go back") }
    }
}

private val PORTA_TEAL = Color(0xFF167D73)
private const val DOOR_TRANSITION_DURATION_MILLIS = 220

@Preview
@Composable
private fun ScenarioPreviewScreenPreview() {
    AppTheme(isDarkMode = true) {
        ScenarioPreviewScreen(
            uiState = ScenarioPreviewUiState(
                actionId = "raise-ielts",
                title = "Raise IELTS from 6.5 to 7.0",
                beforeLabel = "6.5 band",
                afterLabel = "7.0 band",
                openedDoors = listOf(
                    ScenarioDoorUiState(
                        id = "aurora",
                        title = "Aurora Scholars",
                        stateLabel = "Would pass current checks",
                        actualStateLabel = "Needs action",
                        simulatedStateLabel = "Assessed rules passed",
                    ),
                    ScenarioDoorUiState(
                        id = "forge",
                        title = "Forge Student Lab",
                        stateLabel = "Would pass current checks",
                        actualStateLabel = "Needs action",
                        simulatedStateLabel = "Assessed rules passed",
                    ),
                ),
                progressedDoors = listOf(
                    ScenarioDoorUiState(
                        id = "erasmus-mundus",
                        title = "Erasmus Mundus Joint Masters",
                        stateLabel = "Still needs information or source review",
                        actualStateLabel = "Needs action",
                        simulatedStateLabel = "Needs information",
                    ),
                ),
            ),
            onUiEvent = {},
            onNavigateBack = {},
            onBuildProfile = {},
        )
    }
}
