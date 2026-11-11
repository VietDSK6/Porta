package io.github.vietdsk6.porta.presentation.screens.route

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import io.github.vietdsk6.porta.designsystem.theme.AppTheme
import io.github.vietdsk6.porta.domain.model.CostBand
import io.github.vietdsk6.porta.domain.model.EffortLevel

@Preview(name = "Route compact", widthDp = 390, heightDp = 840)
@Composable
private fun RouteCompactPreview() {
    AppTheme { PreviewRouteScreen() }
}

@Preview(name = "Route metro wide", device = "spec:width=1200dp,height=800dp,dpi=160")
@Composable
private fun RouteWidePreview() {
    AppTheme { PreviewRouteScreen() }
}

@Preview(name = "Route dark", widthDp = 390, heightDp = 840)
@Composable
private fun RouteDarkPreview() {
    AppTheme(isDarkMode = true) { PreviewRouteScreen() }
}

@Preview(name = "Route large text", widthDp = 390, heightDp = 840, fontScale = 1.8f)
@Composable
private fun RouteLargeTextPreview() {
    AppTheme { PreviewRouteScreen() }
}

@Composable
private fun PreviewRouteScreen() {
    RouteScreen(
        uiState = previewState,
        onUiEvent = {},
        onOpenNextMoves = {},
        onOpenActivePlan = {},
    )
}

private val previewState = RouteUiState(
    isLoading = false,
    canSave = true,
    routes = listOf(
        RouteOptionUiState(
            id = "english-then-experience",
            steps = listOf(
                RouteStepUiState(
                    id = "ielts",
                    title = "Raise English score to band 7",
                    startDateLabel = "2026-11-11",
                    endDateLabel = "2027-01-10",
                    durationDays = 60,
                    doorsOpened = listOf("Erasmus Mundus Joint Masters", "CERN openlab"),
                    doorsImproved = listOf("Erasmus Mundus Joint Masters", "CERN openlab"),
                    doorsLost = emptyList(),
                    effort = EffortLevel.HIGH,
                    cost = CostBand.LOW,
                ),
                RouteStepUiState(
                    id = "experience",
                    title = "Build six months of relevant experience",
                    startDateLabel = "2027-01-10",
                    endDateLabel = "2027-05-10",
                    durationDays = 120,
                    doorsOpened = listOf("Summer Research Lab"),
                    doorsImproved = listOf("Summer Research Lab"),
                    doorsLost = listOf("Early spring fellowship"),
                    effort = EffortLevel.MEDIUM,
                    cost = CostBand.FREE,
                ),
            ),
            openedTotal = 3,
            openedSaved = 2,
            daysToFirstDoor = 60,
            totalDays = 180,
            costRankSum = 1,
            explanation = "Opens 2 saved doors first.",
        ),
        RouteOptionUiState(
            id = "experience-first",
            steps = listOf(
                RouteStepUiState(
                    id = "experience",
                    title = "Build six months of relevant experience",
                    startDateLabel = "2026-11-11",
                    endDateLabel = "2027-03-11",
                    durationDays = 120,
                    doorsOpened = listOf("Summer Research Lab"),
                    doorsImproved = listOf("Summer Research Lab"),
                    doorsLost = emptyList(),
                    effort = EffortLevel.MEDIUM,
                    cost = CostBand.FREE,
                ),
            ),
            openedTotal = 1,
            openedSaved = 0,
            daysToFirstDoor = 120,
            totalDays = 120,
            costRankSum = 0,
            explanation = "Opens 1 door on the assessed rules.",
        ),
    ),
)
