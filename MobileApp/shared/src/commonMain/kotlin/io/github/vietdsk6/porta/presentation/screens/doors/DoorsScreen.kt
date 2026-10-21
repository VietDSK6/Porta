package io.github.vietdsk6.porta.presentation.screens.doors

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import io.github.vietdsk6.porta.designsystem.theme.AppTheme
import io.github.vietdsk6.porta.domain.model.EligibilityState

@Composable
fun DoorsScreen(
    viewModel: DoorsViewModel,
    onOpenOpportunity: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    DoorsScreen(
        uiState = uiState,
        onUiEvent = viewModel::onUiEvent,
        onOpenOpportunity = onOpenOpportunity,
        modifier = modifier.fillMaxSize(),
    )
}

@Composable
fun DoorsScreen(
    uiState: DoorsUiState,
    onUiEvent: (DoorsUiEvent) -> Unit,
    onOpenOpportunity: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    ScreenWithToolbar(
        modifier = modifier.testTag("doors-screen"),
        isScrollableContent = false,
        title = "Your doors",
        includeBottomInsets = false,
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.verticalListItemSpacingSmall),
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ProfileAndPackContext(
                        isSampleProfile = uiState.isSampleMode,
                        isSampleCatalog = uiState.isSampleCatalog,
                        doorCount = uiState.catalogDoorCount,
                    )
                    Text(
                        text = "See what is open now—and what one honest next step could change.",
                        color = AppTheme.colors.text.secondary,
                        style = AppTheme.typography.bodyLarge,
                    )
                    StateSummary(uiState)
                }
            }
            item {
                DoorFilterRow(
                    selected = uiState.selectedFilter,
                    onSelected = { onUiEvent(DoorsUiEvent.FilterChanged(it)) },
                )
            }
            items(uiState.doors, key = DoorCardUiState::id) { door ->
                DoorCard(
                    door = door,
                    onOpen = { onOpenOpportunity(door.id) },
                    onSavedChanged = { onUiEvent(DoorsUiEvent.SavedChanged(door.id)) },
                )
            }
            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

@Composable
private fun ProfileAndPackContext(
    isSampleProfile: Boolean,
    isSampleCatalog: Boolean,
    doorCount: Int,
) {
    if (!isSampleProfile && !isSampleCatalog) {
        Text(
            text = "$doorCount source-linked opportunities · evaluated on this device",
            color = AppTheme.colors.primary,
            fontWeight = FontWeight.Bold,
            style = AppTheme.typography.bodySmall,
        )
        return
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.alternative),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text(
                when {
                    isSampleProfile && isSampleCatalog -> "SAMPLE PROFILE · SYNTHETIC PACK"
                    isSampleProfile -> "SAMPLE PROFILE · $doorCount SOURCED DOORS"
                    isSampleCatalog -> "YOUR PROFILE · SYNTHETIC PACK"
                    else -> "YOUR PROFILE · $doorCount SOURCED DOORS"
                },
                color = AppTheme.colors.primary,
                fontWeight = FontWeight.Bold,
            )
            Text(
                when {
                    isSampleCatalog -> {
                        "This fallback catalog is fictional and exists only to demonstrate Porta safely."
                    }

                    isSampleProfile -> {
                        "A sample student is evaluated against Porta's offline, source-linked opportunity pack."
                    }

                    else -> {
                        "Your local facts are evaluated against published rules checked in the active offline pack."
                    }
                },
                color = AppTheme.colors.text.primary,
                style = AppTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun StateSummary(uiState: DoorsUiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SummaryCell("${uiState.openCount}", "Open", PORTA_TEAL, Modifier.weight(1f))
        SummaryCell("${uiState.gapCount}", "Has gaps", PORTA_AMBER, Modifier.weight(1f))
        SummaryCell("${uiState.needsInfoCount}", "Verify", AppTheme.colors.primary, Modifier.weight(1f))
    }
}

@Composable
private fun SummaryCell(value: String, label: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surfaceContainer),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(value, color = color, style = AppTheme.typography.h4)
            Text(label, color = AppTheme.colors.text.secondary, style = AppTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun DoorFilterRow(selected: DoorFilter, onSelected: (DoorFilter) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(DoorFilter.entries) { filter ->
            FilterChip(
                selected = filter == selected,
                onClick = { onSelected(filter) },
                label = { Text(filter.label) },
            )
        }
    }
}

@Composable
private fun DoorCard(
    door: DoorCardUiState,
    onOpen: () -> Unit,
    onSavedChanged: () -> Unit,
) {
    val stateColor = door.state.color()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("door-card-${door.id}")
            .clickable(onClick = onOpen),
        shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp, bottomStart = 18.dp, bottomEnd = 18.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surfaceContainer),
        border = BorderStroke(1.dp, stateColor.copy(alpha = 0.72f)),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(door.state.label(), color = stateColor, fontWeight = FontWeight.Bold)
                Text(door.deadlineLabel, color = AppTheme.colors.text.secondary, style = AppTheme.typography.bodySmall)
            }
            Text(door.title, style = AppTheme.typography.h5, color = AppTheme.colors.text.primary)
            Text(door.provider, style = AppTheme.typography.bodyMedium, color = AppTheme.colors.text.secondary)
            Text(door.primaryReason, style = AppTheme.typography.bodyMedium, color = AppTheme.colors.text.primary)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(door.checkedLabel, color = AppTheme.colors.text.secondary, style = AppTheme.typography.bodySmall)
                TextButton(onClick = onSavedChanged) {
                    Text(if (door.isSaved) "Saved" else "Save")
                }
            }
        }
    }
}

private fun EligibilityState.label(): String = when (this) {
    EligibilityState.OPEN -> "OPEN"
    EligibilityState.HAS_GAPS -> "ONE STEP AWAY"
    EligibilityState.NEEDS_INFO -> "VERIFY"
    EligibilityState.NOT_ACTIONABLE -> "NOT ACTIONABLE"
    EligibilityState.CLOSED -> "CLOSED"
}

@Composable
private fun EligibilityState.color(): Color = when (this) {
    EligibilityState.OPEN -> PORTA_TEAL

    EligibilityState.HAS_GAPS -> PORTA_AMBER

    EligibilityState.NEEDS_INFO -> AppTheme.colors.primary

    EligibilityState.NOT_ACTIONABLE,
    EligibilityState.CLOSED,
    -> PORTA_CORAL
}

private val PORTA_TEAL = Color(0xFF2DD4BF)
private val PORTA_AMBER = Color(0xFFF4B860)
private val PORTA_CORAL = Color(0xFFEF6A6A)

@Preview
@Composable
private fun DoorsScreenPreview() {
    AppTheme(isDarkMode = true) {
        DoorsScreen(
            uiState = DoorsUiState(
                isSampleMode = false,
                isSampleCatalog = false,
                catalogDoorCount = 15,
                doors = listOf(
                    DoorCardUiState(
                        id = "kotlin-contest",
                        title = "Kotlin Student Coding Competition",
                        provider = "Kotlin Foundation",
                        state = EligibilityState.NEEDS_INFO,
                        deadlineLabel = "Due 2026-12-07",
                        checkedLabel = "Checked 2026-10-21",
                        isSaved = true,
                        primaryReason = "Confirm full-time enrollment within the published eligibility period.",
                    ),
                ),
                openCount = 0,
                gapCount = 4,
                needsInfoCount = 10,
            ),
            onUiEvent = {},
            onOpenOpportunity = {},
        )
    }
}
