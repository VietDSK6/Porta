package io.github.vietdsk6.porta.presentation.screens.route

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.vietdsk6.porta.designsystem.components.ScreenWithToolbar
import io.github.vietdsk6.porta.designsystem.theme.AppTheme

@Composable
fun RouteScreen(
    viewModel: RouteViewModel,
    onOpenNextMoves: () -> Unit,
    onOpenActivePlan: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    RouteScreen(
        uiState = uiState,
        onUiEvent = viewModel::onUiEvent,
        onOpenNextMoves = onOpenNextMoves,
        onOpenActivePlan = onOpenActivePlan,
        modifier = modifier,
    )
}

@Composable
fun RouteScreen(
    uiState: RouteUiState,
    onUiEvent: (RouteUiEvent) -> Unit,
    onOpenNextMoves: () -> Unit,
    onOpenActivePlan: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ScreenWithToolbar(
        modifier = modifier.fillMaxSize().testTag("route-screen"),
        title = "Route",
        isScrollableContent = false,
        includeBottomInsets = false,
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val wide = maxWidth >= 840.dp && LocalDensity.current.fontScale < 1.5f
            LazyColumn(
                modifier = Modifier.fillMaxSize().testTag("route-list"),
                contentPadding = PaddingValues(horizontal = if (wide) 28.dp else 16.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                item { RouteHero(uiState.selectedRoute) }
                if (uiState.routes.size > 1) {
                    item { RouteChoices(uiState, onUiEvent) }
                }
                val selected = uiState.selectedRoute
                when {
                    uiState.isLoading -> item {
                        Text("Finding routes through the assessed rules…", modifier = Modifier.testTag("route-loading"))
                    }

                    selected == null -> {
                        item { NoRouteCard() }
                        item { RouteControls(uiState, onUiEvent) }
                    }

                    else -> {
                        item {
                            Text(
                                selected.summary,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite }
                                    .testTag("route-summary"),
                            )
                        }
                        item {
                            if (wide) {
                                RouteMetroMap(route = selected)
                            } else {
                                RouteTimeline(steps = selected.steps)
                            }
                        }
                        item { RouteControls(uiState, onUiEvent) }
                        item {
                            RouteActions(
                                uiState = uiState,
                                onUiEvent = onUiEvent,
                                onOpenNextMoves = onOpenNextMoves,
                                onOpenActivePlan = onOpenActivePlan,
                            )
                        }
                    }
                }
                if (uiState.error != null || uiState.saveNotice != null) {
                    item {
                        RouteNotice(
                            message = uiState.error ?: uiState.saveNotice.orEmpty(),
                            onDismiss = { onUiEvent(RouteUiEvent.NoticeDismissed) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RouteHero(route: RouteOptionUiState?) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("THE PATH AHEAD", style = MaterialTheme.typography.labelMedium, color = AppTheme.colors.primary)
        Text(
            "See the steps that could open real doors.",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            RouteMetric(route?.openedTotal?.toString() ?: "—", "Doors", Modifier.weight(1f))
            RouteMetric(route?.steps?.size?.toString() ?: "—", "Steps", Modifier.weight(1f))
            RouteMetric(route?.totalDays?.toString() ?: "—", "Days", Modifier.weight(1f))
        }
        Text(
            route?.explanation ?: "Porta plans from published checks and your chosen limits.",
            style = MaterialTheme.typography.bodyMedium,
            color = AppTheme.colors.text.secondary,
        )
    }
}

@Composable
private fun RouteMetric(value: String, label: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelSmall, color = AppTheme.colors.text.secondary)
        }
    }
}

@Composable
private fun RouteChoices(uiState: RouteUiState, onUiEvent: (RouteUiEvent) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Choose a route", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            uiState.routes.forEachIndexed { index, route ->
                FilterChip(
                    selected = uiState.selectedRoute?.id == route.id,
                    onClick = { onUiEvent(RouteUiEvent.RouteSelected(route.id)) },
                    label = { Text("Route ${index + 1}") },
                    modifier = Modifier.testTag("route-choice-$index"),
                )
            }
        }
    }
}

@Composable
private fun NoRouteCard() {
    Card(modifier = Modifier.fillMaxWidth().testTag("route-empty")) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("No route fits these limits yet", style = MaterialTheme.typography.titleMedium)
            Text(
                "Try a longer horizon, a higher budget, or more effort. Porta only uses milestones tied to assessed checks.",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun RouteActions(
    uiState: RouteUiState,
    onUiEvent: (RouteUiEvent) -> Unit,
    onOpenNextMoves: () -> Unit,
    onOpenActivePlan: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = { onUiEvent(RouteUiEvent.SaveRoute) },
                enabled = uiState.canSave && !uiState.isSaving,
                modifier = Modifier.testTag("route-save"),
            ) {
                Text(if (uiState.isSaving) "Saving…" else "Save route")
            }
            OutlinedButton(onClick = onOpenNextMoves) { Text("First move") }
        }
        if (!uiState.canSave) {
            Text("Set up a local profile before saving this route.", style = MaterialTheme.typography.bodySmall)
        }
        TextButton(onClick = onOpenActivePlan) { Text("View active plan") }
        Text(
            "Porta shows what the encoded rules would allow. Verify live deadlines, costs, and manual checks with each provider.",
            style = MaterialTheme.typography.bodySmall,
            color = AppTheme.colors.text.secondary,
        )
    }
}

@Composable
private fun RouteNotice(message: String, onDismiss: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(message, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
            TextButton(onClick = onDismiss) { Text("Dismiss") }
        }
    }
}
