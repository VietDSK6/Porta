package io.github.vietdsk6.porta.presentation.screens.studentprofile

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
fun StudentProfileScreen(
    modifier: Modifier = Modifier,
    viewModel: StudentProfileViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    StudentProfileScreen(
        modifier = modifier.fillMaxSize(),
        uiState = uiState,
        onUiEvent = viewModel::onUiEvent,
    )
}

@Composable
fun StudentProfileScreen(
    modifier: Modifier = Modifier,
    uiState: StudentProfileUiState,
    onUiEvent: (StudentProfileUiEvent) -> Unit,
) {
    ScreenWithToolbar(
        modifier = modifier,
        isScrollableContent = true, // Set to false if content itself has scrollable content such as LazyColumn
        title = "StudentProfileScreen",
        includeBottomInsets = true, // Set to false if bottom nav is visible
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sectionSpacing)) {
            Text("StudentProfileScreen")
        }
    }
}
