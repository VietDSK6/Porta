package io.github.vietdsk6.porta.presentation.screens.doors

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import io.github.vietdsk6.porta.domain.model.EligibilityState
import kotlinx.coroutines.delay

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
    LaunchedEffect(uiState.highlightedDoorIds) {
        if (uiState.highlightedDoorIds.isNotEmpty()) {
            delay(DOOR_HIGHLIGHT_DURATION_MILLIS)
            onUiEvent(DoorsUiEvent.DismissHighlights)
        }
    }
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
                        sourceReviewDueCount = uiState.sourceReviewDueCount,
                    )
                    Text(
                        text = "See how your current profile compares with each published check.",
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
                    isHighlighted = door.id in uiState.highlightedDoorIds,
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
    sourceReviewDueCount: Int,
) {
    if (!isSampleProfile && !isSampleCatalog) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "$doorCount source-linked opportunities · checked on this device",
                color = AppTheme.colors.primary,
                fontWeight = FontWeight.Bold,
                style = AppTheme.typography.bodySmall,
            )
            if (sourceReviewDueCount > 0) {
                Text(
                    text = "$sourceReviewDueCount source records need a Porta catalog review. Open a door to confirm the live source.",
                    color = PORTA_AMBER,
                    style = AppTheme.typography.bodySmall,
                )
            }
        }
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            when {
                isSampleProfile && isSampleCatalog -> "SAMPLE PROFILE · SYNTHETIC PACK"
                isSampleProfile -> "SAMPLE PROFILE · $doorCount SOURCED DOORS"
                isSampleCatalog -> "YOUR PROFILE · SYNTHETIC PACK"
                else -> "YOUR PROFILE · $doorCount SOURCED DOORS"
            },
            color = AppTheme.colors.primary,
            fontWeight = FontWeight.Bold,
            style = AppTheme.typography.bodySmall,
        )
        Text(
            when {
                isSampleCatalog -> "The fallback catalog is fictional and is only used for a safe demo."
                isSampleProfile -> "A sample student is compared with the active source-linked pack."
                else -> "Your saved facts are compared with the active offline pack."
            },
            color = AppTheme.colors.text.secondary,
            style = AppTheme.typography.bodyMedium,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StateSummary(uiState: DoorsUiState) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = AppTheme.colors.surfaceContainer,
        shape = RoundedCornerShape(12.dp),
    ) {
        FlowRow(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SummaryItem(uiState.openCount, "ASSESSED RULES PASSED", PORTA_TEAL)
            SummaryItem(uiState.gapCount, "NEEDS ACTION", PORTA_AMBER)
            SummaryItem(uiState.needsInfoCount, "NEEDS INFORMATION", AppTheme.colors.primary)
            if (uiState.notAvailableCount > 0) {
                SummaryItem(uiState.notAvailableCount, "NOT AVAILABLE THIS CYCLE", PORTA_CORAL)
            }
            if (uiState.closedCount > 0) {
                SummaryItem(uiState.closedCount, "CLOSED", PORTA_CORAL)
            }
            if (uiState.archivedCount > 0) {
                SummaryItem(uiState.archivedCount, "ARCHIVED", AppTheme.colors.text.secondary)
            }
        }
    }
}

@Composable
private fun SummaryItem(value: Int, label: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Text(value.toString(), color = color, fontWeight = FontWeight.Bold)
        Text(label, color = AppTheme.colors.text.secondary, style = AppTheme.typography.bodySmall)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DoorFilterRow(selected: DoorFilter, onSelected: (DoorFilter) -> Unit) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        DoorFilter.entries.forEach { filter ->
            FilterChip(
                modifier = Modifier
                    .defaultMinSize(minHeight = 48.dp)
                    .testTag("door-filter-${filter.name}"),
                selected = filter == selected,
                onClick = { onSelected(filter) },
                label = { Text(filter.label, style = AppTheme.typography.bodySmall) },
            )
        }
    }
}

@Composable
private fun DoorCard(
    door: DoorCardUiState,
    isHighlighted: Boolean,
    onOpen: () -> Unit,
    onSavedChanged: () -> Unit,
) {
    val stateColor = door.state.color()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("door-card-${door.id}")
            .clickable(role = Role.Button, onClick = onOpen),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isHighlighted) AppTheme.colors.alternative else AppTheme.colors.surfaceContainer,
        ),
        border = if (isHighlighted) BorderStroke(1.dp, AppTheme.colors.primary) else null,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            if (isHighlighted) {
                Text(
                    text = "UPDATED FROM YOUR CONFIRMED PROFILE",
                    color = AppTheme.colors.primary,
                    fontWeight = FontWeight.Bold,
                    style = AppTheme.typography.bodySmall,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        door.stateLabel,
                        color = stateColor,
                        fontWeight = FontWeight.Bold,
                        style = AppTheme.typography.bodySmall,
                    )
                    Text(
                        door.title,
                        style = AppTheme.typography.h5,
                        color = AppTheme.colors.text.primary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                IconButton(
                    modifier = Modifier
                        .size(40.dp)
                        .semantics {
                            contentDescription = if (door.isSaved) {
                                "Remove ${door.title} from saved opportunities"
                            } else {
                                "Save ${door.title}"
                            }
                        },
                    onClick = onSavedChanged,
                ) {
                    Text(
                        text = if (door.isSaved) "★" else "☆",
                        color = if (door.isSaved) AppTheme.colors.primary else AppTheme.colors.text.secondary,
                        style = AppTheme.typography.h5,
                    )
                }
            }
            Text(
                listOf(door.provider, door.categoryLabel).filter(String::isNotBlank).joinToString(" · "),
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.colors.text.secondary,
            )
            Text(
                door.primaryReason,
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.colors.text.primary,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                door.deadlineLabel,
                color = AppTheme.colors.text.secondary,
                fontWeight = FontWeight.SemiBold,
                style = AppTheme.typography.bodySmall,
            )
            Text(
                door.checkedLabel,
                color = if (door.sourceReviewNeedsAttention) PORTA_AMBER else AppTheme.colors.text.secondary,
                fontWeight = if (door.sourceReviewNeedsAttention) FontWeight.SemiBold else FontWeight.Normal,
                style = AppTheme.typography.bodySmall,
            )
            if (door.isSaved) {
                Text(
                    "SAVED",
                    color = AppTheme.colors.primary,
                    fontWeight = FontWeight.Bold,
                    style = AppTheme.typography.bodySmall,
                )
            }
        }
    }
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

private val PORTA_TEAL = Color(0xFF167D73)
private val PORTA_AMBER = Color(0xFFA66B1F)
private val PORTA_CORAL = Color(0xFFB84F5B)
private const val DOOR_HIGHLIGHT_DURATION_MILLIS = 2_600L

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
                        categoryLabel = "Student contest",
                        state = EligibilityState.NEEDS_INFO,
                        stateLabel = "NEEDS INFORMATION",
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
