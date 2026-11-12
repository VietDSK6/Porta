package io.github.vietdsk6.porta.presentation.screens.scenariopreview

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.vietdsk6.porta.designsystem.components.ScreenWithToolbar
import io.github.vietdsk6.porta.designsystem.generated.resources.UiRes
import io.github.vietdsk6.porta.designsystem.generated.resources.ic_back
import io.github.vietdsk6.porta.designsystem.theme.AppTheme
import io.github.vietdsk6.porta.generated.resources.*
import io.github.vietdsk6.porta.generated.resources.Res
import io.github.vietdsk6.porta.presentation.components.porta.ActionImpactNodeUiState
import io.github.vietdsk6.porta.presentation.components.porta.ActionRipple
import io.github.vietdsk6.porta.presentation.components.porta.ImpactOutcome
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun ScenarioPreviewScreen(
    viewModel: ScenarioPreviewViewModel,
    onNavigateBack: () -> Unit,
    onPlanSaved: () -> Unit,
    onBuildProfile: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            viewModel.onUiEvent(ScenarioPreviewUiEvent.SaveHandled)
            onPlanSaved()
        }
    }
    ScenarioPreviewScreen(
        uiState = uiState,
        onUiEvent = viewModel::onUiEvent,
        onNavigateBack = onNavigateBack,
        onBuildProfile = onBuildProfile,
        onOpenSource = { url -> if (url.startsWith("https://")) runCatching { uriHandler.openUri(url) } },
        modifier = modifier.fillMaxSize(),
    )
}

@Composable
fun ScenarioPreviewScreen(
    uiState: ScenarioPreviewUiState,
    onUiEvent: (ScenarioPreviewUiEvent) -> Unit,
    onNavigateBack: () -> Unit,
    onBuildProfile: () -> Unit,
    onOpenSource: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var selectedDoorId by rememberSaveable(uiState.actionId) { mutableStateOf<String?>(null) }
    var showRipple by rememberSaveable(uiState.actionId) { mutableStateOf(false) }
    ScreenWithToolbar(
        modifier = modifier.testTag("scenario-preview-screen"),
        isScrollableContent = false,
        title = stringResource(Res.string.scenario_title),
        titleContent = { Text(stringResource(Res.string.scenario_title), style = AppTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold) },
        includeBottomInsets = true,
        navigationIcon = UiRes.drawable.ic_back,
        onNavigationIconClick = onNavigateBack,
    ) {
        if (uiState.error != null) {
            ErrorState(uiState.localizedError(), onNavigateBack)
            return@ScreenWithToolbar
        }
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val wide = maxWidth >= 960.dp && LocalDensity.current.fontScale < 1.5f
            if (wide) {
                DesktopScenarioContent(
                    uiState = uiState,
                    selectedDoorId = selectedDoorId ?: uiState.affectedDoors.firstOrNull()?.id,
                    onSelectDoor = { selectedDoorId = it },
                    onUiEvent = onUiEvent,
                    onNavigateBack = onNavigateBack,
                    onBuildProfile = onBuildProfile,
                    onOpenSource = onOpenSource,
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().testTag("scenario-preview-content"),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    item { SimulationBanner() }
                    item { ScenarioOutcomeSummary(uiState) }
                    item { FactTransition(uiState) }
                    if (uiState.affectedDoors.isNotEmpty()) {
                        item {
                            TextButton(modifier = Modifier.testTag("scenario-ripple-toggle"), onClick = { showRipple = !showRipple }) {
                                Text(stringResource(if (showRipple) Res.string.scenario_hide_ripple else Res.string.scenario_view_ripple))
                            }
                        }
                        if (showRipple) item { ScenarioImpactPath(uiState) }
                        item { Text(stringResource(Res.string.scenario_results), modifier = Modifier.semantics { heading() }, style = AppTheme.typography.h6) }
                        items(uiState.affectedDoors, key = ScenarioDoorUiState::id) { door ->
                            ScenarioResultRow(door, selectedDoorId == door.id, onSelect = {
                                selectedDoorId = if (selectedDoorId == door.id) null else door.id
                            })
                            if (selectedDoorId == door.id) ScenarioSelectedDetail(door, onOpenSource)
                        }
                    } else {
                        item { NoChangedDoorsCard() }
                    }
                    item { UnchangedCount(uiState) }
                    item { ScenarioActions(uiState, onUiEvent, onNavigateBack, onBuildProfile) }
                }
            }
        }
    }
}

@Composable
private fun DesktopScenarioContent(
    uiState: ScenarioPreviewUiState,
    selectedDoorId: String?,
    onSelectDoor: (String) -> Unit,
    onUiEvent: (ScenarioPreviewUiEvent) -> Unit,
    onNavigateBack: () -> Unit,
    onBuildProfile: () -> Unit,
    onOpenSource: (String) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxSize().testTag("scenario-desktop-workspace"),
        horizontalArrangement = Arrangement.spacedBy(28.dp),
    ) {
        LazyColumn(
            modifier = Modifier.weight(0.46f).fillMaxHeight().testTag("scenario-desktop-overview"),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item { SimulationBanner() }
            item { ScenarioOutcomeSummary(uiState) }
            item { FactTransition(uiState) }
            if (uiState.affectedDoors.isNotEmpty()) {
                item { ScenarioImpactPath(uiState) }
            } else {
                item { NoChangedDoorsCard() }
            }
        }
        Column(
            modifier = Modifier.weight(0.54f).fillMaxHeight().testTag("scenario-desktop-results"),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(stringResource(Res.string.scenario_results), modifier = Modifier.semantics { heading() }, style = AppTheme.typography.h6)
            LazyColumn(modifier = Modifier.weight(0.48f).testTag("scenario-desktop-result-list"), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(uiState.affectedDoors, key = ScenarioDoorUiState::id) { door ->
                    ScenarioResultRow(door, selectedDoorId == door.id, onSelect = { onSelectDoor(door.id) })
                }
            }
            LazyColumn(modifier = Modifier.weight(0.52f).testTag("scenario-desktop-selected-detail"), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                selectedDoorId?.let { selected ->
                    uiState.affectedDoors.firstOrNull { it.id == selected }?.let { door ->
                        item { ScenarioSelectedDetail(door, onOpenSource) }
                    }
                }
                item { UnchangedCount(uiState) }
                item { ScenarioActions(uiState, onUiEvent, onNavigateBack, onBuildProfile) }
            }
        }
    }
}

@Composable
private fun ScenarioActions(
    uiState: ScenarioPreviewUiState,
    onUiEvent: (ScenarioPreviewUiEvent) -> Unit,
    onNavigateBack: () -> Unit,
    onBuildProfile: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        val concerns = uiState.affectedDoors.count { it.timingWarning != null }
        if (concerns > 0) {
            Text(
                pluralStringResource(Res.plurals.scenario_timing_concerns, concerns, concerns),
                color = AppTheme.colors.status.warning,
                style = AppTheme.typography.bodySmall,
            )
        }
        Button(
            modifier = Modifier.fillMaxWidth().testTag("scenario-save-plan"),
            enabled = !uiState.isSaving,
            onClick = {
                if (uiState.isSampleProfile) onBuildProfile() else onUiEvent(ScenarioPreviewUiEvent.SavePlan)
            },
        ) {
            Text(
                when {
                    uiState.isSampleProfile -> stringResource(Res.string.scenario_build_profile)
                    uiState.isSaving -> stringResource(Res.string.scenario_saving)
                    else -> stringResource(Res.string.scenario_add_milestone)
                },
            )
        }
        if (uiState.saveError != null || uiState.saveErrorCode != null) {
            Text(uiState.localizedSaveError(), color = AppTheme.colors.status.error, style = AppTheme.typography.bodyMedium)
        }
        OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = onNavigateBack) { Text(stringResource(Res.string.scenario_discard)) }
    }
}

@Composable
private fun UnchangedCount(uiState: ScenarioPreviewUiState) {
    if (uiState.unchangedDoors.isNotEmpty()) {
        Text(
            pluralStringResource(Res.plurals.scenario_unchanged, uiState.unchangedDoors.size, uiState.unchangedDoors.size),
            color = AppTheme.colors.text.secondary,
            style = AppTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun ScenarioImpactPath(uiState: ScenarioPreviewUiState) {
    val currentState = stringResource(Res.string.scenario_current_state)
    val nodes = mutableListOf<ActionImpactNodeUiState>()
    for (door in uiState.affectedDoors) {
        nodes += door.toImpactNode(
            when (door.outcome) {
                ScenarioDoorOutcome.CURRENT_CHECKS_PASSED -> ImpactOutcome.CURRENT_CHECKS_PASSED
                ScenarioDoorOutcome.CHECKED_PROGRESS -> ImpactOutcome.CHECKED_PROGRESS
                ScenarioDoorOutcome.NEEDS_VERIFICATION -> ImpactOutcome.NEEDS_VERIFICATION
            },
            currentState,
        )
    }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (uiState.scopeNotice != null || uiState.hasRuleScopeNotice) {
            Text(
                text = uiState.localizedScopeNotice(),
                modifier = Modifier.testTag("scenario-scope-notice"),
                color = AppTheme.colors.primary,
                style = AppTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
        ActionRipple(
            actionLabel = uiState.title,
            actualLabel = uiState.localizedBefore(),
            simulatedLabel = uiState.afterLabel,
            nodes = nodes,
            overflowDestination = stringResource(Res.string.scenario_results),
        )
    }
}

@Composable
private fun ScenarioDoorUiState.toImpactNode(outcome: ImpactOutcome, currentState: String): ActionImpactNodeUiState {
    val actual = localizedActualState().ifBlank { currentState }
    val simulated = localizedSimulatedState()
    val visibleSimulated = if (simulated.isBlank()) localizedOutcome() else simulated
    return ActionImpactNodeUiState(
        id = id,
        title = title,
        statusLabel = localizedOutcome(),
        transitionLabel = "$actual → $visibleSimulated",
        outcome = outcome,
        timingWarning = if (timingWarning != null) localizedTimingWarning() else null,
    )
}

@Composable
private fun ScenarioOutcomeSummary(uiState: ScenarioPreviewUiState) {
    val affected = uiState.affectedDoors
    val changedChecks = affected.sumOf { it.changedRules.size }
    val needsVerification = affected.count { it.outcome == ScenarioDoorOutcome.NEEDS_VERIFICATION }
    val timingConcerns = affected.count { it.timingWarning != null }
    val headline = if (affected.isEmpty()) {
        stringResource(Res.string.scenario_no_checks_change)
    } else {
        pluralStringResource(Res.plurals.scenario_opportunities_affected, affected.size, affected.size)
    }
    val changedAnnouncement = pluralStringResource(Res.plurals.scenario_checks_could_change, changedChecks, changedChecks)
    val verificationAnnouncement = if (needsVerification > 0) {
        pluralStringResource(Res.plurals.scenario_need_verification, needsVerification, needsVerification)
    } else {
        null
    }
    val timingAnnouncement = if (timingConcerns > 0) {
        pluralStringResource(Res.plurals.scenario_timing_count, timingConcerns, timingConcerns)
    } else {
        null
    }
    val announcement = listOfNotNull(changedAnnouncement, verificationAnnouncement, timingAnnouncement).joinToString(" · ")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("scenario-outcome-summary")
            .semantics { liveRegion = LiveRegionMode.Polite },
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = headline,
            modifier = Modifier.semantics { heading() },
            style = AppTheme.typography.h5,
        )
        Text(
            text = if (announcement.isBlank()) stringResource(Res.string.scenario_profile_unchanged) else announcement,
            color = AppTheme.colors.text.secondary,
            style = AppTheme.typography.bodyMedium,
        )
        if (uiState.hasCatalogCounts) {
            Text(
                text = uiState.aggregateTransitionLabel(),
                modifier = Modifier.testTag("scenario-aggregate-transition"),
                color = AppTheme.colors.text.secondary,
                style = AppTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun ScenarioPreviewUiState.aggregateTransitionLabel(): String = listOf(
    stringResource(Res.string.scenario_aggregate_passed, baselineCounts.currentChecksPassed, simulatedCounts.currentChecksPassed),
    stringResource(Res.string.scenario_aggregate_action, baselineCounts.needsAction, simulatedCounts.needsAction),
    stringResource(Res.string.scenario_aggregate_info, baselineCounts.needsInformation, simulatedCounts.needsInformation),
    stringResource(Res.string.scenario_aggregate_unavailable, baselineCounts.notAvailable, simulatedCounts.notAvailable),
    stringResource(Res.string.scenario_aggregate_closed, baselineCounts.closed, simulatedCounts.closed),
).joinToString(" · ")

@Composable
private fun SimulationBanner() {
    Surface(
        modifier = Modifier.fillMaxWidth().testTag("scenario-simulation-banner"),
        color = AppTheme.colors.alternative,
        shape = RoundedCornerShape(12.dp),
    ) {
        Text(
            text = stringResource(Res.string.scenario_simulation_notice),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
            color = AppTheme.colors.onAlternative,
            fontWeight = FontWeight.SemiBold,
            style = AppTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun FactTransition(uiState: ScenarioPreviewUiState) {
    Column(
        modifier = Modifier.fillMaxWidth().testTag("scenario-fact-transition"),
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Text(uiState.title, style = AppTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
        Text(
            stringResource(Res.string.scenario_fact_hypothetical, uiState.localizedBefore(), uiState.afterLabel),
            color = AppTheme.colors.primary,
            style = AppTheme.typography.bodyMedium,
        )
        if (uiState.scopeNotice != null || uiState.hasRuleScopeNotice) {
            Text(
                uiState.localizedScopeNotice(),
                modifier = Modifier.testTag("scenario-scope-notice"),
                color = AppTheme.colors.text.secondary,
                style = AppTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun ScenarioResultRow(door: ScenarioDoorUiState, selected: Boolean, onSelect: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().testTag("scenario-result-${door.id}"),
        color = AppTheme.colors.surfaceContainer,
        shape = RoundedCornerShape(12.dp),
        border = if (selected) BorderStroke(1.dp, AppTheme.colors.primary) else null,
    ) {
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    door.title,
                    modifier = Modifier.weight(1f).testTag("scenario-door-title-${door.id}"),
                    style = AppTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                TextButton(modifier = Modifier.testTag("scenario-details-${door.id}"), onClick = onSelect) {
                    Text(stringResource(if (selected) Res.string.scenario_hide else Res.string.scenario_details))
                }
            }
            Text(
                stringResource(
                    Res.string.scenario_state_transition,
                    if (door.actualStateLabel.isBlank() && door.actualState == null) stringResource(Res.string.scenario_current_state) else door.localizedActualState(),
                    if (door.simulatedStateLabel.isBlank() && door.simulatedState == null) door.localizedOutcome() else door.localizedSimulatedState(),
                ),
                color = AppTheme.colors.text.secondary,
                style = AppTheme.typography.bodySmall,
            )
            Text(door.localizedOutcome(), color = door.outcome.color(), style = AppTheme.typography.bodySmall)
            if (door.timingWarning != null) {
                Text(stringResource(Res.string.scenario_timing_warning), color = AppTheme.colors.status.warning, style = AppTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun ScenarioSelectedDetail(door: ScenarioDoorUiState, onOpenSource: (String) -> Unit) {
    var showEvidence by rememberSaveable(door.id) { mutableStateOf(false) }
    Column(
        modifier = Modifier.fillMaxWidth().testTag("scenario-selected-${door.id}").padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (door.changedRules.isNotEmpty()) {
            Text(stringResource(Res.string.scenario_changed_checks), style = AppTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            door.changedRules.forEach { change ->
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(change.explanation, style = AppTheme.typography.bodyMedium)
                    Text(stringResource(Res.string.scenario_state_transition, change.localizedBefore(), change.localizedAfter()), color = AppTheme.colors.primary, style = AppTheme.typography.bodySmall)
                }
            }
        }
        if (door.remainingBlockers.isNotEmpty() || door.remainingBlockerDetails.isNotEmpty()) {
            Text(stringResource(Res.string.scenario_what_remains), style = AppTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            if (door.remainingBlockerDetails.isNotEmpty()) {
                door.remainingBlockerDetails.forEach { blocker -> Text("• ${blocker.localizedText()}", color = AppTheme.colors.text.secondary, style = AppTheme.typography.bodySmall) }
            } else {
                door.remainingBlockers.forEach { blocker -> Text("• $blocker", color = AppTheme.colors.text.secondary, style = AppTheme.typography.bodySmall) }
            }
        }
        if (door.timingWarning != null) {
            Text(door.localizedTimingWarning(), color = AppTheme.colors.status.warning, style = AppTheme.typography.bodySmall)
        }
        TextButton(modifier = Modifier.testTag("scenario-evidence-toggle-${door.id}"), onClick = { showEvidence = !showEvidence }) {
            Text(stringResource(if (showEvidence) Res.string.scenario_hide_evidence else Res.string.scenario_review_evidence))
        }
        if (showEvidence) ScenarioEvidence(door, onOpenSource)
    }
}

@Composable
private fun ScenarioEvidence(door: ScenarioDoorUiState, onOpenSource: (String) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().testTag("scenario-evidence-${door.id}"),
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        HorizontalDivider(color = AppTheme.colors.outline.copy(alpha = 0.3f))
        door.changedRules.forEach { change ->
            Text(stringResource(Res.string.scenario_check_result, change.localizedResultTransition()), color = AppTheme.colors.text.secondary, style = AppTheme.typography.bodySmall)
            if (change.sourceNote.isNotBlank()) Text(change.sourceNote, color = AppTheme.colors.text.secondary, style = AppTheme.typography.bodySmall)
        }
        if (door.timingLabel.isNotBlank() || door.deadline != null) Text(stringResource(Res.string.scenario_planning_timing, door.localizedTiming()), color = AppTheme.colors.text.secondary, style = AppTheme.typography.bodySmall)
        if (door.lastCheckedLabel.isNotBlank() || door.lastCheckedDate != null) Text(door.localizedChecked(), color = AppTheme.colors.text.secondary, style = AppTheme.typography.bodySmall)
        if (door.sourceSection.isNotBlank()) Text(stringResource(Res.string.scenario_section, door.sourceSection), color = AppTheme.colors.text.secondary, style = AppTheme.typography.bodySmall)
        if (door.sourceUrl.startsWith("https://")) {
            TextButton(modifier = Modifier.testTag("scenario-source-${door.id}"), onClick = { onOpenSource(door.sourceUrl) }) {
                Text(stringResource(Res.string.scenario_open_source))
            }
        }
    }
}

@Composable
private fun ScenarioDoorOutcome.color(): Color = when (this) {
    ScenarioDoorOutcome.CURRENT_CHECKS_PASSED -> AppTheme.colors.status.success
    ScenarioDoorOutcome.CHECKED_PROGRESS -> AppTheme.colors.primary
    ScenarioDoorOutcome.NEEDS_VERIFICATION -> AppTheme.colors.status.warning
}

@Composable
private fun NoChangedDoorsCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = AppTheme.colors.surfaceContainer,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, AppTheme.colors.outline.copy(alpha = 0.35f)),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(stringResource(Res.string.scenario_no_check_changes), fontWeight = FontWeight.SemiBold)
            Text(
                text = stringResource(Res.string.scenario_no_change_caveat),
                color = AppTheme.colors.text.secondary,
                style = AppTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun ErrorState(message: String, onNavigateBack: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(stringResource(Res.string.scenario_unavailable), style = AppTheme.typography.h5)
        Text(message, color = AppTheme.colors.text.secondary)
        OutlinedButton(onClick = onNavigateBack) { Text(stringResource(Res.string.scenario_go_back)) }
    }
}

@Preview
@Composable
private fun ScenarioPreviewScreenLightPreview() {
    ScenarioPreviewContentPreview(isDarkMode = false)
}

@Preview
@Composable
private fun ScenarioPreviewScreenPreview() {
    ScenarioPreviewContentPreview(isDarkMode = true)
}

@Composable
private fun ScenarioPreviewContentPreview(isDarkMode: Boolean) {
    AppTheme(isDarkMode = isDarkMode) {
        ScenarioPreviewScreen(
            uiState = ScenarioPreviewUiState(
                actionId = "raise-ielts",
                title = "Raise IELTS from 6.5 to 7.0",
                beforeLabel = "6.5 band",
                afterLabel = "7.0 band",
                affectedDoors = listOf(
                    ScenarioDoorUiState(
                        id = "aurora",
                        title = "Aurora Scholars",
                        stateLabel = "Would pass current checks",
                        outcome = ScenarioDoorOutcome.CURRENT_CHECKS_PASSED,
                        actualStateLabel = "Needs action",
                        simulatedStateLabel = "Assessed rules passed",
                        changedRules = listOf(ScenarioRuleChangeUiState(id = "aurora-ielts", explanation = "IELTS threshold", beforeLabel = "6.5", afterLabel = "7.0", resultTransitionLabel = "Needs action → Check passed")),
                    ),
                    ScenarioDoorUiState(
                        id = "forge",
                        title = "Forge Student Lab",
                        stateLabel = "Would pass current checks",
                        outcome = ScenarioDoorOutcome.CURRENT_CHECKS_PASSED,
                        actualStateLabel = "Needs action",
                        simulatedStateLabel = "Assessed rules passed",
                        changedRules = listOf(ScenarioRuleChangeUiState(id = "forge-ielts", explanation = "IELTS threshold", beforeLabel = "6.5", afterLabel = "7.0", resultTransitionLabel = "Needs action → Check passed")),
                    ),
                    ScenarioDoorUiState(
                        id = "erasmus-mundus",
                        title = "Erasmus Mundus Joint Masters",
                        stateLabel = "Still needs information or source review",
                        outcome = ScenarioDoorOutcome.NEEDS_VERIFICATION,
                        actualStateLabel = "Needs action",
                        simulatedStateLabel = "Needs information",
                        changedRules = listOf(ScenarioRuleChangeUiState(id = "erasmus-ielts", explanation = "IELTS threshold", beforeLabel = "6.5", afterLabel = "7.0", resultTransitionLabel = "Needs action → Check passed")),
                    ),
                ),
            ),
            onUiEvent = {},
            onNavigateBack = {},
            onBuildProfile = {},
        )
    }
}
