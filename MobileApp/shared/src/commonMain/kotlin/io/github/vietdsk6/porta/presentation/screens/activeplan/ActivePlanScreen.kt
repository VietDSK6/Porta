package io.github.vietdsk6.porta.presentation.screens.activeplan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.vietdsk6.porta.designsystem.components.ScreenWithToolbar
import io.github.vietdsk6.porta.designsystem.theme.AppTheme

@Composable
fun ActivePlanScreen(
    modifier: Modifier = Modifier,
    viewModel: ActivePlanViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ActivePlanScreen(
        modifier = modifier.fillMaxSize(),
        uiState = uiState,
        onUiEvent = viewModel::onUiEvent,
    )
}

@Composable
fun ActivePlanScreen(
    modifier: Modifier = Modifier,
    uiState: ActivePlanUiState,
    onUiEvent: (ActivePlanUiEvent) -> Unit,
) {
    ScreenWithToolbar(
        modifier = modifier,
        isScrollableContent = true, // Set to false if content itself has scrollable content such as LazyColumn
        title = "ActivePlanScreen",
        includeBottomInsets = true, // Set to false if bottom nav is visible
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sectionSpacing)) {
            Text("ActivePlanScreen")
        }
    }
}
