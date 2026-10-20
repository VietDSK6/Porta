package io.github.vietdsk6.porta.presentation.screens.scenariopreview

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
    onSavePlan: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ScenarioPreviewScreen(
        uiState = uiState,
        onUiEvent = viewModel::onUiEvent,
        onNavigateBack = onNavigateBack,
        onSavePlan = onSavePlan,
        modifier = modifier.fillMaxSize(),
    )
}

@Composable
fun ScenarioPreviewScreen(
    uiState: ScenarioPreviewUiState,
    onUiEvent: (ScenarioPreviewUiEvent) -> Unit,
    onNavigateBack: () -> Unit,
    onSavePlan: () -> Unit,
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("WHAT-IF · SAMPLE", color = AppTheme.colors.primary, fontWeight = FontWeight.Bold)
                    Text(
                        "Nothing here changes your actual profile.",
                        color = AppTheme.colors.text.secondary,
                        style = AppTheme.typography.bodyLarge,
                    )
                    BeforeAfterCard(uiState)
                }
            }
            item {
                Text(
                    "${uiState.openedDoors.size} doors open",
                    color = PORTA_TEAL,
                    style = AppTheme.typography.h5,
                )
            }
            items(uiState.openedDoors, key = ScenarioDoorUiState::id) { door ->
                ScenarioDoorCard(door = door, changed = true)
            }
            if (uiState.unchangedDoors.isNotEmpty()) {
                item { Text("Unchanged", style = AppTheme.typography.h6) }
                items(uiState.unchangedDoors, key = ScenarioDoorUiState::id) { door ->
                    ScenarioDoorCard(door = door, changed = false)
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            onUiEvent(ScenarioPreviewUiEvent.Acknowledged)
                            onSavePlan()
                        },
                    ) {
                        Text("Add milestone to plan")
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
private fun BeforeAfterCard(uiState: ScenarioPreviewUiState) {
    Card(
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surfaceContainer),
        border = BorderStroke(1.dp, AppTheme.colors.primary),
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(uiState.title, style = AppTheme.typography.h5)
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
private fun ScenarioDoorCard(door: ScenarioDoorUiState, changed: Boolean) {
    val accent = if (changed) PORTA_TEAL else AppTheme.colors.outline
    Card(
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surfaceContainer),
        border = BorderStroke(1.dp, accent),
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp, bottomStart = 14.dp, bottomEnd = 14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(door.title, modifier = Modifier.weight(1f), style = AppTheme.typography.bodyLarge)
            Text(door.stateLabel, color = accent, fontWeight = FontWeight.Bold)
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

private val PORTA_TEAL = Color(0xFF2DD4BF)

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
                    ScenarioDoorUiState("aurora", "Aurora Scholars", "Opens"),
                    ScenarioDoorUiState("forge", "Forge Student Lab", "Opens"),
                ),
            ),
            onUiEvent = {},
            onNavigateBack = {},
            onSavePlan = {},
        )
    }
}
