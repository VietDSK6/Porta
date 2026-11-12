package io.github.vietdsk6.porta.presentation.screens.doors

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.vietdsk6.porta.designsystem.components.ScreenWithToolbar
import io.github.vietdsk6.porta.designsystem.theme.AppTheme
import io.github.vietdsk6.porta.generated.resources.*
import io.github.vietdsk6.porta.presentation.navigation.DesktopCommand
import io.github.vietdsk6.porta.presentation.navigation.LocalDesktopCommand
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource

private const val DOOR_HIGHLIGHT_DURATION_MILLIS = 2_600L

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
    val searchFocusRequester = remember { FocusRequester() }
    val desktopRequest = LocalDesktopCommand.current
    LaunchedEffect(desktopRequest) {
        if (desktopRequest?.command == DesktopCommand.FocusSearch) {
            searchFocusRequester.requestFocus()
            desktopRequest.onHandled()
        }
    }
    LaunchedEffect(uiState.highlightedDoorIds) {
        if (uiState.highlightedDoorIds.isNotEmpty()) {
            delay(DOOR_HIGHLIGHT_DURATION_MILLIS)
            onUiEvent(DoorsUiEvent.DismissHighlights)
        }
    }
    ScreenWithToolbar(
        modifier = modifier.testTag("doors-screen"),
        isScrollableContent = false,
        title = stringResource(Res.string.doors_title),
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
                        isDemoProfile = uiState.isDemoProfile,
                        isSampleCatalog = uiState.isSampleCatalog,
                        doorCount = uiState.catalogDoorCount,
                        gapCount = uiState.gapCount,
                        sourceReviewDueCount = uiState.sourceReviewDueCount,
                    )
                }
            }
            item {
                DoorCatalogControls(
                    searchQuery = uiState.searchQuery,
                    selectedCategory = uiState.selectedCategory,
                    selectedDeadlineFilter = uiState.selectedDeadlineFilter,
                    onSearchChanged = { onUiEvent(DoorsUiEvent.SearchChanged(it)) },
                    onCategoryChanged = { onUiEvent(DoorsUiEvent.CategoryChanged(it)) },
                    onDeadlineChanged = { onUiEvent(DoorsUiEvent.DeadlineFilterChanged(it)) },
                    searchFocusRequester = searchFocusRequester,
                )
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
                        hasCatalogCriteria = uiState.searchQuery.isNotBlank() ||
                            uiState.selectedCategory != null ||
                            uiState.selectedDeadlineFilter != DoorDeadlineFilter.ALL,
                        onShowAll = {
                            onUiEvent(DoorsUiEvent.SearchChanged(""))
                            onUiEvent(DoorsUiEvent.CategoryChanged(null))
                            onUiEvent(DoorsUiEvent.DeadlineFilterChanged(DoorDeadlineFilter.ALL))
                            onUiEvent(DoorsUiEvent.FilterChanged(DoorFilter.ALL))
                        },
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
