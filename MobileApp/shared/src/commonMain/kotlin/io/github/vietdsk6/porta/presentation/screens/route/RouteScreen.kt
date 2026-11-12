package io.github.vietdsk6.porta.presentation.screens.route

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import io.github.vietdsk6.porta.generated.resources.Res
import io.github.vietdsk6.porta.generated.resources.route_choice_label
import io.github.vietdsk6.porta.generated.resources.route_choose
import io.github.vietdsk6.porta.generated.resources.route_days
import io.github.vietdsk6.porta.generated.resources.route_demo_badge
import io.github.vietdsk6.porta.generated.resources.route_dismiss
import io.github.vietdsk6.porta.generated.resources.route_doors_passing
import io.github.vietdsk6.porta.generated.resources.route_empty_help
import io.github.vietdsk6.porta.generated.resources.route_empty_title
import io.github.vietdsk6.porta.generated.resources.route_first_move
import io.github.vietdsk6.porta.generated.resources.route_hero_eyebrow
import io.github.vietdsk6.porta.generated.resources.route_hero_title
import io.github.vietdsk6.porta.generated.resources.route_hide_why
import io.github.vietdsk6.porta.generated.resources.route_illustrative_badge
import io.github.vietdsk6.porta.generated.resources.route_loading
import io.github.vietdsk6.porta.generated.resources.route_milestones
import io.github.vietdsk6.porta.generated.resources.route_no_open_doors
import io.github.vietdsk6.porta.generated.resources.route_open_doors
import io.github.vietdsk6.porta.generated.resources.route_profile_to_save
import io.github.vietdsk6.porta.generated.resources.route_save
import io.github.vietdsk6.porta.generated.resources.route_saving
import io.github.vietdsk6.porta.generated.resources.route_toolbar_title
import io.github.vietdsk6.porta.generated.resources.route_verify_sources
import io.github.vietdsk6.porta.generated.resources.route_view_active_plan
import io.github.vietdsk6.porta.generated.resources.route_why_numbers
import io.github.vietdsk6.porta.presentation.theme.PortaBreakpoints
import io.github.vietdsk6.porta.presentation.theme.PortaDimens
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

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
    var showWhy by remember(uiState.selectedRoute?.id) { mutableStateOf(false) }
    ScreenWithToolbar(
        modifier = modifier.fillMaxSize().testTag("route-screen"),
        title = stringResource(Res.string.route_toolbar_title),
        isScrollableContent = false,
        includeBottomInsets = false,
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val wide = maxWidth >= PortaBreakpoints.wide &&
                LocalDensity.current.fontScale < PortaBreakpoints.maxWideFontScale
            LazyColumn(
                modifier = Modifier.fillMaxSize().testTag("route-list"),
                contentPadding = PaddingValues(
                    vertical = PortaDimens.sectionGap,
                ),
                verticalArrangement = Arrangement.spacedBy(PortaDimens.sectionGap),
            ) {
                item { RouteHero(uiState) }
                if (uiState.routes.size > 1) {
                    item { RouteChoices(uiState, onUiEvent) }
                }
                val selected = uiState.selectedRoute
                when {
                    uiState.isLoading -> item {
                        Text(stringResource(Res.string.route_loading), modifier = Modifier.testTag("route-loading"))
                    }

                    selected == null -> {
                        item { NoRouteCard() }
                        item { RouteControls(uiState, onUiEvent) }
                    }

                    else -> {
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(
                                    stringResource(Res.string.route_milestones),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                if (wide) {
                                    RouteMetroMap(route = selected)
                                } else {
                                    RouteTimeline(steps = selected.steps)
                                }
                            }
                        }
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                TextButton(
                                    onClick = { showWhy = !showWhy },
                                    modifier = Modifier.testTag("route-why"),
                                ) {
                                    Text(stringResource(if (showWhy) Res.string.route_hide_why else Res.string.route_why_numbers))
                                }
                                if (showWhy) {
                                    Text(
                                        routeSummary(selected),
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.fillMaxWidth()
                                            .semantics { liveRegion = LiveRegionMode.Polite }
                                            .testTag("route-summary"),
                                    )
                                    Text(
                                        routeReason(selected.reason),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = AppTheme.colors.text.secondary,
                                    )
                                }
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
                val notice = uiState.notice
                if (notice != null) {
                    item {
                        RouteNotice(
                            message = routeNotice(notice),
                            onDismiss = { onUiEvent(RouteUiEvent.NoticeDismissed) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RouteHero(uiState: RouteUiState) {
    val route = uiState.selectedRoute
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(stringResource(Res.string.route_hero_eyebrow), style = MaterialTheme.typography.labelMedium, color = AppTheme.colors.primary)
        if (uiState.isIllustrativePreview) {
            Text(stringResource(Res.string.route_illustrative_badge), style = MaterialTheme.typography.labelMedium)
        } else if (uiState.isFictionalProfile) {
            Text(stringResource(Res.string.route_demo_badge), style = MaterialTheme.typography.labelMedium)
        }
        Text(
            when {
                route == null -> stringResource(Res.string.route_hero_title)
                route.openedTotal == 0 -> stringResource(Res.string.route_no_open_doors)
                else -> pluralStringResource(Res.plurals.route_open_doors, route.openedTotal, route.openedTotal)
            },
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.testTag("route-headline"),
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            RouteMetric(route?.openedTotal?.toString() ?: "—", stringResource(Res.string.route_doors_passing), Modifier.weight(1f))
            RouteMetric(route?.steps?.size?.toString() ?: "—", stringResource(Res.string.route_milestones), Modifier.weight(1f))
            RouteMetric(route?.totalDays?.toString() ?: "—", stringResource(Res.string.route_days), Modifier.weight(1f))
        }
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
        Text(stringResource(Res.string.route_choose), style = MaterialTheme.typography.titleMedium)
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            uiState.routes.forEachIndexed { index, route ->
                FilterChip(
                    selected = uiState.selectedRoute?.id == route.id,
                    onClick = { onUiEvent(RouteUiEvent.RouteSelected(route.id)) },
                    label = { Text(stringResource(Res.string.route_choice_label, index + 1)) },
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
            Text(stringResource(Res.string.route_empty_title), style = MaterialTheme.typography.titleMedium)
            Text(
                stringResource(Res.string.route_empty_help),
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
                Text(stringResource(if (uiState.isSaving) Res.string.route_saving else Res.string.route_save))
            }
            OutlinedButton(onClick = onOpenNextMoves) { Text(stringResource(Res.string.route_first_move)) }
        }
        if (!uiState.canSave) {
            Text(stringResource(Res.string.route_profile_to_save), style = MaterialTheme.typography.bodySmall)
        }
        TextButton(onClick = onOpenActivePlan) { Text(stringResource(Res.string.route_view_active_plan)) }
        Text(
            stringResource(Res.string.route_verify_sources),
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
            TextButton(onClick = onDismiss) { Text(stringResource(Res.string.route_dismiss)) }
        }
    }
}
