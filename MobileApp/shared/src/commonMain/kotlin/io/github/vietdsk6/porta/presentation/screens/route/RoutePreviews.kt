package io.github.vietdsk6.porta.presentation.screens.route

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.vietdsk6.porta.designsystem.theme.AppTheme
import io.github.vietdsk6.porta.domain.model.CostBand
import io.github.vietdsk6.porta.domain.model.EffortLevel
import io.github.vietdsk6.porta.presentation.locale.LocalAppLocale

@Preview(name = "Route compact", widthDp = 390, heightDp = 840)
@Composable
private fun RouteCompactPreview() {
    AppTheme { PreviewRouteScreen() }
}

@Preview(name = "Route zero fully open", widthDp = 390, heightDp = 840)
@Composable
private fun RouteZeroOpenPreview() {
    AppTheme {
        RouteScreen(
            uiState = RouteUiState(
                isLoading = false,
                isFictionalProfile = true,
                routes = listOf(
                    RouteOptionUiState(
                        id = "degree-progress",
                        steps = listOf(
                            RouteStepUiState(
                                id = "degree",
                                title = "Complete the required first degree",
                                startDateLabel = "2026-11-11",
                                endDateLabel = "2027-05-10",
                                durationDays = 180,
                                doorsOpened = emptyList(),
                                doorsImproved = listOf("Erasmus Mundus Joint Masters", "DAAD EPOS", "Schwarzman Scholars"),
                                doorsLost = listOf("Kotlin Student Coding Competition"),
                                effort = EffortLevel.HIGH,
                                cost = CostBand.MEDIUM,
                            ),
                        ),
                        openedTotal = 0,
                        openedSaved = 0,
                        daysToFirstDoor = null,
                        totalDays = 180,
                        costRankSum = CostBand.MEDIUM.rank,
                        reason = RouteReason(RouteReasonKind.IMPROVES, 3),
                    ),
                ),
            ),
            onUiEvent = {},
            onOpenNextMoves = {},
            onOpenActivePlan = {},
        )
    }
}

@Preview(name = "Route controls compact", widthDp = 320, heightDp = 840)
@Composable
private fun RouteControlsCompactPreview() {
    AppTheme {
        Column(Modifier.padding(16.dp)) {
            RouteControls(
                uiState = RouteUiState(isLoading = false),
                onUiEvent = {},
            )
        }
    }
}

@Preview(name = "Route compact Vietnamese", widthDp = 390, heightDp = 840, locale = "vi")
@Composable
private fun RouteVietnamesePreview() {
    CompositionLocalProvider(LocalAppLocale provides "vi") {
        AppTheme { PreviewRouteScreen() }
    }
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
    canSave = false,
    isFictionalProfile = true,
    isIllustrativePreview = true,
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
                    doorsOpened = listOf("Aurora Scholars", "Horizon Exchange"),
                    doorsImproved = listOf("Aurora Scholars", "Horizon Exchange"),
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
                    doorsOpened = listOf("Forge Student Lab"),
                    doorsImproved = listOf("Forge Student Lab"),
                    doorsLost = listOf("Local Makers Challenge"),
                    effort = EffortLevel.MEDIUM,
                    cost = CostBand.FREE,
                ),
            ),
            openedTotal = 3,
            openedSaved = 2,
            daysToFirstDoor = 60,
            totalDays = 180,
            costRankSum = 1,
            reason = RouteReason(RouteReasonKind.OPENS_SAVED, 2),
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
                    doorsOpened = listOf("Forge Student Lab"),
                    doorsImproved = listOf("Forge Student Lab"),
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
            reason = RouteReason(RouteReasonKind.OPENS, 1),
        ),
    ),
)
