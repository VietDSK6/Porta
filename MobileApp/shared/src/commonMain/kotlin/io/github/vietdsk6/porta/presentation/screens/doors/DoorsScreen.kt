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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import io.github.vietdsk6.porta.designsystem.components.porta.PortaArch
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
                        gapCount = uiState.gapCount,
                        sourceReviewDueCount = uiState.sourceReviewDueCount,
                    )
                }
            }
            item {
                DoorFilterRow(
                    selected = uiState.selectedFilter,
                    onSelected = { onUiEvent(DoorsUiEvent.FilterChanged(it)) },
                )
            }
            if (uiState.doors.isEmpty()) {
                item {
                    DoorsEmptyState(
                        selectedFilter = uiState.selectedFilter,
                        onShowAll = { onUiEvent(DoorsUiEvent.FilterChanged(DoorFilter.ALL)) },
                    )
                }
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
    gapCount: Int,
    sourceReviewDueCount: Int,
) {
    val summary = "$doorCount ${if (doorCount == 1) "opportunity" else "opportunities"} · $gapCount need action"
    if (!isSampleProfile && !isSampleCatalog) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = summary,
                color = AppTheme.colors.primary,
                fontWeight = FontWeight.Bold,
                style = AppTheme.typography.bodySmall,
            )
            if (sourceReviewDueCount > 0) {
                Text(
                    text = "$sourceReviewDueCount source records need review.",
                    color = AppTheme.colors.status.warning,
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
                isSampleProfile -> "SAMPLE PROFILE"
                isSampleCatalog -> "YOUR PROFILE · SYNTHETIC PACK"
                else -> "YOUR PROFILE"
            },
            color = AppTheme.colors.primary,
            fontWeight = FontWeight.Bold,
            style = AppTheme.typography.bodySmall,
        )
        Text(
            summary,
            color = AppTheme.colors.text.secondary,
            style = AppTheme.typography.bodyMedium,
        )
        if (isSampleCatalog) {
            Text("Fictional opportunities for demo only.", color = AppTheme.colors.text.secondary, style = AppTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun DoorsEmptyState(
    selectedFilter: DoorFilter,
    onShowAll: () -> Unit,
) {
    val title = when (selectedFilter) {
        DoorFilter.SAVED -> "No saved opportunities yet"
        DoorFilter.ALL -> "No opportunities in this pack"
        else -> "No matches for this filter"
    }
    val body = when (selectedFilter) {
        DoorFilter.SAVED -> "Save opportunities to compare and revisit them here."
        DoorFilter.ALL -> "Import or activate an opportunity pack to start comparing published checks."
        else -> "Your opportunities are still here. Clear the filter to see every current result."
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("doors-empty-${selectedFilter.name.lowercase()}")
            .padding(horizontal = 12.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        PortaArch(
            modifier = Modifier.size(width = 64.dp, height = 76.dp),
            containerColor = AppTheme.colors.alternative,
            outlineColor = AppTheme.colors.outline,
        ) {
            Text(
                text = if (selectedFilter == DoorFilter.SAVED) "☆" else "—",
                color = AppTheme.colors.primary,
                style = AppTheme.typography.h5,
            )
        }
        Text(title, color = AppTheme.colors.text.primary, style = AppTheme.typography.h5)
        Text(
            text = body,
            color = AppTheme.colors.text.secondary,
            style = AppTheme.typography.bodyMedium,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
        if (selectedFilter != DoorFilter.ALL) {
            TextButton(
                modifier = Modifier
                    .defaultMinSize(minHeight = 48.dp)
                    .testTag("doors-empty-show-all"),
                onClick = onShowAll,
            ) {
                Text("Show all opportunities")
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DoorFilterRow(selected: DoorFilter, onSelected: (DoorFilter) -> Unit) {
    val primaryFilters = listOf(DoorFilter.ALL, DoorFilter.GAPS, DoorFilter.VERIFY, DoorFilter.SAVED)
    val secondaryFilters = DoorFilter.entries.filterNot(primaryFilters::contains)
    var showMore by rememberSaveable { mutableStateOf(selected in secondaryFilters) }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            primaryFilters.forEach { filter -> DoorFilterChip(filter, selected, onSelected) }
        }
        TextButton(
            modifier = Modifier.defaultMinSize(minHeight = 48.dp),
            onClick = { showMore = !showMore },
        ) {
            Text(if (showMore) "Fewer filters" else "More filters")
        }
        if (showMore) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                secondaryFilters.forEach { filter -> DoorFilterChip(filter, selected, onSelected) }
            }
        }
    }
}

@Composable
private fun DoorFilterChip(
    filter: DoorFilter,
    selected: DoorFilter,
    onSelected: (DoorFilter) -> Unit,
) {
    FilterChip(
        modifier = Modifier
            .defaultMinSize(minHeight = 48.dp)
            .testTag("door-filter-${filter.name}"),
        selected = filter == selected,
        onClick = { onSelected(filter) },
        label = {
            Text(
                when (filter) {
                    DoorFilter.ALL -> "All"
                    DoorFilter.GAPS -> "Action"
                    DoorFilter.VERIFY -> "Verify"
                    DoorFilter.SAVED -> "Saved"
                    else -> filter.label.lowercase().replaceFirstChar(Char::uppercaseChar)
                },
                style = AppTheme.typography.bodySmall,
            )
        },
    )
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
            verticalArrangement = Arrangement.spacedBy(5.dp),
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
                        .size(48.dp)
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
                text = door.primaryReason,
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.colors.text.primary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                door.deadlineLabel,
                color = AppTheme.colors.text.secondary,
                fontWeight = FontWeight.SemiBold,
                style = AppTheme.typography.bodySmall,
            )
            if (door.sourceReviewNeedsAttention) {
                Text(
                    door.checkedLabel,
                    color = AppTheme.colors.status.warning,
                    fontWeight = FontWeight.SemiBold,
                    style = AppTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
private fun EligibilityState.color(): Color = when (this) {
    EligibilityState.OPEN -> AppTheme.colors.status.success

    EligibilityState.HAS_GAPS -> AppTheme.colors.status.warning

    EligibilityState.NEEDS_INFO -> AppTheme.colors.primary

    EligibilityState.NOT_ACTIONABLE,
    EligibilityState.CLOSED,
    -> AppTheme.colors.status.error
}

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

@Preview
@Composable
private fun SavedDoorsEmptyPreview() {
    AppTheme {
        DoorsScreen(
            uiState = DoorsUiState(
                isSampleMode = false,
                isSampleCatalog = false,
                catalogDoorCount = 15,
                selectedFilter = DoorFilter.SAVED,
                gapCount = 4,
                needsInfoCount = 10,
            ),
            onUiEvent = {},
            onOpenOpportunity = {},
        )
    }
}
