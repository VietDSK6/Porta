package io.github.vietdsk6.porta.presentation.screens.doors

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import io.github.vietdsk6.porta.designsystem.theme.AppTheme
import io.github.vietdsk6.porta.domain.model.EligibilityState

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
