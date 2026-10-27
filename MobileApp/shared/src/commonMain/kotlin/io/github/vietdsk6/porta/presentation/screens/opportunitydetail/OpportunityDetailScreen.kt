package io.github.vietdsk6.porta.presentation.screens.opportunitydetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.vietdsk6.porta.designsystem.components.ScreenWithToolbar
import io.github.vietdsk6.porta.designsystem.generated.resources.UiRes
import io.github.vietdsk6.porta.designsystem.generated.resources.ic_back
import io.github.vietdsk6.porta.designsystem.theme.AppTheme
import io.github.vietdsk6.porta.domain.model.EligibilityState
import io.github.vietdsk6.porta.domain.model.FactKey
import io.github.vietdsk6.porta.domain.model.RuleResult

@Composable
fun OpportunityDetailScreen(
    viewModel: OpportunityDetailViewModel,
    onNavigateBack: () -> Unit,
    onPreviewAction: (String) -> Unit,
    onEditProfile: (FactKey, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current
    OpportunityDetailScreen(
        uiState = uiState,
        onUiEvent = viewModel::onUiEvent,
        onNavigateBack = onNavigateBack,
        onPreviewAction = onPreviewAction,
        onEditProfile = onEditProfile,
        onOpenSource = { url ->
            if (url.startsWith("https://")) {
                runCatching { uriHandler.openUri(url) }
            }
        },
        modifier = modifier.fillMaxSize(),
    )
}

@Composable
fun OpportunityDetailScreen(
    uiState: OpportunityDetailUiState,
    onUiEvent: (OpportunityDetailUiEvent) -> Unit,
    onNavigateBack: () -> Unit,
    onPreviewAction: (String) -> Unit,
    onEditProfile: (FactKey, String) -> Unit = { _, _ -> },
    onOpenSource: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    ScreenWithToolbar(
        modifier = modifier.testTag("opportunity-receipt-screen"),
        isScrollableContent = false,
        title = "Opportunity details",
        includeBottomInsets = true,
        navigationIcon = UiRes.drawable.ic_back,
        onNavigationIconClick = onNavigateBack,
    ) {
        if (uiState.error != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                Column(
                    modifier = Modifier.fillMaxWidth().widthIn(max = 720.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text("Opportunity unavailable", style = AppTheme.typography.h5)
                    Text(uiState.error, color = AppTheme.colors.text.secondary)
                    OutlinedButton(onClick = onNavigateBack) { Text("Go back") }
                }
            }
            return@ScreenWithToolbar
        }
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val useWideLayout = maxWidth >= WIDE_DETAIL_BREAKPOINT && LocalDensity.current.fontScale < 1.5f
            if (useWideLayout) {
                WideOpportunityDetailContent(
                    uiState = uiState,
                    onUiEvent = onUiEvent,
                    onPreviewAction = onPreviewAction,
                    onEditProfile = onEditProfile,
                    onOpenSource = onOpenSource,
                )
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                    CompactOpportunityDetailContent(
                        uiState = uiState,
                        onUiEvent = onUiEvent,
                        onPreviewAction = onPreviewAction,
                        onEditProfile = onEditProfile,
                        onOpenSource = onOpenSource,
                        modifier = Modifier.fillMaxSize().widthIn(max = 760.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun CompactOpportunityDetailContent(
    uiState: OpportunityDetailUiState,
    onUiEvent: (OpportunityDetailUiEvent) -> Unit,
    onPreviewAction: (String) -> Unit,
    onEditProfile: (FactKey, String) -> Unit,
    onOpenSource: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.testTag("opportunity-detail-content"),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        item { OpportunityHeader(uiState) }
        item { CurrentConclusion(uiState) }
        item {
            if (uiState.rules.any { it.result == RuleResult.FAIL }) {
                NeedsActionSection(uiState)
            }
        }
        item {
            if (uiState.nextStepGuidance.isNotBlank() || uiState.relatedActionId != null) {
                SuggestedNextMoveSection(
                    uiState = uiState,
                    onUiEvent = onUiEvent,
                    onPreviewAction = onPreviewAction,
                )
            }
        }
        item {
            if (uiState.informationTasks.isNotEmpty()) {
                InformationTasksSection(
                    tasks = uiState.informationTasks,
                    onEditProfile = onEditProfile,
                    onOpenSource = { onOpenSource(uiState.sourceUrl) },
                )
            }
        }
        item {
            if (uiState.rules.any { it.result in setOf(RuleResult.UNKNOWN, RuleResult.REVIEW) }) {
                NeedsInformationSection(uiState)
            }
        }
        item { EvidenceDisclosure(uiState) }
        item { SourceDetails(uiState = uiState, onOpenSource = onOpenSource) }
    }
}

@Composable
private fun WideOpportunityDetailContent(
    uiState: OpportunityDetailUiState,
    onUiEvent: (OpportunityDetailUiEvent) -> Unit,
    onPreviewAction: (String) -> Unit,
    onEditProfile: (FactKey, String) -> Unit,
    onOpenSource: (String) -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 1280.dp)
                .testTag("opportunity-detail-desktop-workspace"),
            horizontalArrangement = Arrangement.spacedBy(32.dp),
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(0.44f)
                    .testTag("opportunity-detail-primary-pane"),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                item { OpportunityHeader(uiState) }
                item { CurrentConclusion(uiState) }
                if (uiState.rules.any { it.result == RuleResult.FAIL }) {
                    item { NeedsActionSection(uiState) }
                }
                item {
                    if (uiState.nextStepGuidance.isNotBlank() || uiState.relatedActionId != null) {
                        SuggestedNextMoveSection(
                            uiState = uiState,
                            onUiEvent = onUiEvent,
                            onPreviewAction = onPreviewAction,
                        )
                    }
                }
                if (uiState.informationTasks.isNotEmpty()) {
                    item {
                        InformationTasksSection(
                            tasks = uiState.informationTasks,
                            onEditProfile = onEditProfile,
                            onOpenSource = { onOpenSource(uiState.sourceUrl) },
                        )
                    }
                }
            }
            LazyColumn(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(0.56f)
                    .testTag("opportunity-detail-evidence-pane"),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                if (uiState.rules.any { it.result in setOf(RuleResult.UNKNOWN, RuleResult.REVIEW) }) {
                    item { NeedsInformationSection(uiState) }
                }
                item { EvidenceDisclosure(uiState) }
                item { SourceDetails(uiState = uiState, onOpenSource = onOpenSource) }
            }
        }
    }
}

@Composable
private fun NeedsActionSection(uiState: OpportunityDetailUiState) {
    RuleSection(
        title = "Needs action",
        rules = uiState.rules.filter { it.result == RuleResult.FAIL },
        accent = AppTheme.colors.status.warning,
    )
}

@Composable
private fun NeedsInformationSection(uiState: OpportunityDetailUiState) {
    RuleSection(
        title = "Needs verification",
        rules = uiState.rules.filter { it.result in setOf(RuleResult.UNKNOWN, RuleResult.REVIEW) },
        accent = AppTheme.colors.primary,
    )
}

@Composable
private fun PassedChecksSection(uiState: OpportunityDetailUiState) {
    RuleSection(
        title = "Passed checks",
        rules = uiState.rules.filter { it.result == RuleResult.PASS },
        accent = AppTheme.colors.status.success,
    )
}

@Composable
private fun SuggestedNextMoveSection(
    uiState: OpportunityDetailUiState,
    onUiEvent: (OpportunityDetailUiEvent) -> Unit,
    onPreviewAction: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = if (uiState.relatedActionId == null) "What to verify next" else "Suggested next move",
            modifier = Modifier.semantics { heading() },
            style = AppTheme.typography.h6,
        )
        Text(
            uiState.nextStepGuidance,
            color = AppTheme.colors.text.secondary,
            style = AppTheme.typography.bodyMedium,
        )
        uiState.relatedActionId?.let { actionId ->
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    onUiEvent(OpportunityDetailUiEvent.Acknowledged)
                    onPreviewAction(actionId)
                },
            ) {
                Text(uiState.relatedActionTitle.ifBlank { "Preview this profile change" })
            }
        }
    }
}

@Composable
private fun InformationTasksSection(
    tasks: List<InformationTaskUiState>,
    onEditProfile: (FactKey, String) -> Unit,
    onOpenSource: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Information tasks",
            modifier = Modifier.semantics { heading() },
            style = AppTheme.typography.h6,
        )
        Text(
            "These tasks only fill missing information. They do not improve a check until you save a real answer and Porta evaluates it.",
            color = AppTheme.colors.text.secondary,
            style = AppTheme.typography.bodyMedium,
        )
        tasks.forEach { task ->
            Surface(
                modifier = Modifier.fillMaxWidth().testTag("information-task-${task.id}"),
                color = AppTheme.colors.alternative,
                shape = RoundedCornerShape(14.dp),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(task.title, fontWeight = FontWeight.Bold, style = AppTheme.typography.bodyLarge)
                    Text(task.question, color = AppTheme.colors.text.primary, style = AppTheme.typography.bodyMedium)
                    if (task.sourceNote.isNotBlank()) {
                        Text(
                            task.sourceNote,
                            color = AppTheme.colors.text.secondary,
                            style = AppTheme.typography.bodySmall,
                        )
                    }
                    OutlinedButton(
                        onClick = when (task.destination) {
                            InformationTaskDestination.ACADEMIC_PROFILE -> {
                                { onEditProfile(task.factKey, task.question) }
                            }

                            InformationTaskDestination.OFFICIAL_SOURCE -> onOpenSource
                        },
                    ) {
                        Text(
                            when (task.destination) {
                                InformationTaskDestination.ACADEMIC_PROFILE -> "Update academic profile"
                                InformationTaskDestination.OFFICIAL_SOURCE -> "Check official source"
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OpportunityHeader(uiState: OpportunityDetailUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            when {
                uiState.isSampleProfile && uiState.isSampleCatalog -> "SAMPLE PROFILE · SYNTHETIC PACK"
                uiState.isSampleProfile -> "SAMPLE PROFILE · SOURCED PACK"
                uiState.isSampleCatalog -> "YOUR PROFILE · SYNTHETIC PACK"
                else -> "YOUR PROFILE · SOURCED PACK"
            },
            color = AppTheme.colors.primary,
            fontWeight = FontWeight.Bold,
        )
        Text(uiState.title, style = AppTheme.typography.h4)
        Text(uiState.provider, color = AppTheme.colors.text.secondary, style = AppTheme.typography.bodyLarge)
        Text(uiState.deadlineLabel, color = AppTheme.colors.text.secondary)
    }
}

@Composable
private fun CurrentConclusion(uiState: OpportunityDetailUiState) {
    Surface(
        color = AppTheme.colors.alternative,
        shape = RoundedCornerShape(14.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = "Current conclusion",
                modifier = Modifier.semantics { heading() },
                style = AppTheme.typography.h6,
            )
            Text(
                uiState.stateLabel,
                color = uiState.stateColor(),
                fontWeight = FontWeight.Bold,
                style = AppTheme.typography.bodySmall,
            )
            Text(uiState.conclusionSummary, color = AppTheme.colors.text.primary)
            Text(uiState.timingLabel, color = AppTheme.colors.text.secondary, style = AppTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun RuleSection(
    title: String,
    rules: List<RuleReceiptUiState>,
    accent: Color,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = title,
            modifier = Modifier.semantics { heading() },
            style = AppTheme.typography.h6,
            color = AppTheme.colors.text.primary,
        )
        rules.forEachIndexed { index, rule ->
            RuleRow(rule = rule, accent = accent)
            if (index != rules.lastIndex) {
                HorizontalDivider(color = AppTheme.colors.outline.copy(alpha = 0.3f))
            }
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun RuleRow(rule: RuleReceiptUiState, accent: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(rule.explanation, style = AppTheme.typography.bodyLarge, color = AppTheme.colors.text.primary)
        if (rule.result == RuleResult.REVIEW) {
            Text(
                text = "Programme-specific confirmation · ${rule.expectedLabel}",
                color = accent,
                fontWeight = FontWeight.SemiBold,
                style = AppTheme.typography.bodyMedium,
            )
        } else {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = rule.actualLabel,
                    color = accent,
                    fontWeight = FontWeight.SemiBold,
                    style = AppTheme.typography.bodyMedium,
                )
                Text("→", color = AppTheme.colors.text.secondary)
                Text(
                    text = rule.expectedLabel,
                    fontWeight = FontWeight.SemiBold,
                    style = AppTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun EvidenceDisclosure(uiState: OpportunityDetailUiState) {
    var expanded by rememberSaveable(uiState.id) { mutableStateOf(false) }
    val passedRules = uiState.rules.filter { it.result == RuleResult.PASS }
    val sourceNotes = uiState.rules.map(RuleReceiptUiState::sourceNote).filter(String::isNotBlank).distinct()
    if (passedRules.isEmpty() && sourceNotes.isEmpty() && uiState.softCriteria.isEmpty()) return
    Column(
        modifier = Modifier.fillMaxWidth().testTag("opportunity-evidence"),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Evidence behind this conclusion",
                modifier = Modifier.semantics { heading() },
                style = AppTheme.typography.h6,
            )
            Text(
                text = buildList {
                    if (passedRules.isNotEmpty()) {
                        add("${passedRules.size} passed ${if (passedRules.size == 1) "check" else "checks"}")
                    }
                    if (sourceNotes.isNotEmpty()) {
                        add("${sourceNotes.size} rule ${if (sourceNotes.size == 1) "note" else "notes"}")
                    }
                    if (uiState.softCriteria.isNotEmpty()) {
                        add("${uiState.softCriteria.size} non-scoring selection factors")
                    }
                }.joinToString(" · "),
                color = AppTheme.colors.text.secondary,
                style = AppTheme.typography.bodyMedium,
            )
        }
        TextButton(
            modifier = Modifier.testTag("opportunity-evidence-toggle"),
            onClick = { expanded = !expanded },
        ) {
            Text(if (expanded) "Hide evidence" else "Review evidence")
        }
        if (expanded) {
            if (passedRules.isNotEmpty()) {
                PassedChecksSection(uiState)
            }
            if (uiState.softCriteria.isNotEmpty()) {
                SoftSelectionFactors(uiState.softCriteria)
            }
            if (sourceNotes.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Rule source notes", style = AppTheme.typography.h6)
                    sourceNotes.forEach { note ->
                        Text("• $note", color = AppTheme.colors.text.secondary)
                    }
                }
            }
        }
    }
}

@Composable
private fun SoftSelectionFactors(criteria: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Soft selection factors",
            modifier = Modifier.semantics { heading() },
            style = AppTheme.typography.h6,
        )
        Text(
            "These may affect selection, but Porta never treats them as pass/fail rules.",
            color = AppTheme.colors.text.secondary,
            style = AppTheme.typography.bodyMedium,
        )
        criteria.forEach { criterion -> Text("• $criterion", color = AppTheme.colors.text.primary) }
    }
}

@Composable
private fun SourceDetails(
    uiState: OpportunityDetailUiState,
    onOpenSource: (String) -> Unit,
) {
    var expanded by rememberSaveable(uiState.id) { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Source and last review",
            modifier = Modifier.semantics { heading() },
            style = AppTheme.typography.h6,
        )
        Text(
            uiState.checkedLabel,
            color = if (uiState.sourceReviewNeedsAttention) {
                AppTheme.colors.status.warning
            } else {
                AppTheme.colors.text.primary
            },
            fontWeight = if (uiState.sourceReviewNeedsAttention) FontWeight.SemiBold else FontWeight.Normal,
        )
        Text(uiState.disclaimer, color = AppTheme.colors.text.secondary, style = AppTheme.typography.bodyMedium)
        TextButton(
            modifier = Modifier.testTag("opportunity-source-toggle"),
            onClick = { expanded = !expanded },
        ) {
            Text(if (expanded) "Hide source details" else "Source details")
        }
        if (expanded) {
            if (uiState.sourceSection.isNotBlank()) {
                Text("Section: ${uiState.sourceSection}", color = AppTheme.colors.text.primary)
            }
            if (uiState.isArchived) {
                Text(
                    "Archived records are retained for reference and are excluded from current planning.",
                    color = AppTheme.colors.text.secondary,
                    style = AppTheme.typography.bodyMedium,
                )
            }
            if (uiState.canOpenSource) {
                TextButton(onClick = { onOpenSource(uiState.sourceUrl) }) {
                    Text("Open official source")
                }
            } else {
                Text("Official source unavailable", color = AppTheme.colors.text.secondary)
            }
        }
    }
}

@Composable
private fun OpportunityDetailUiState.stateColor(): Color = when (state) {
    EligibilityState.OPEN -> AppTheme.colors.status.success

    EligibilityState.HAS_GAPS -> AppTheme.colors.status.warning

    EligibilityState.NEEDS_INFO -> AppTheme.colors.primary

    EligibilityState.NOT_ACTIONABLE,
    EligibilityState.CLOSED,
    null,
    -> AppTheme.colors.status.error
}

private val WIDE_DETAIL_BREAKPOINT = 960.dp

@Preview
@Composable
private fun OpportunityDetailScreenPreview() {
    AppTheme(isDarkMode = true) {
        OpportunityDetailScreen(
            uiState = previewOpportunityDetailState,
            onUiEvent = {},
            onNavigateBack = {},
            onPreviewAction = {},
            onEditProfile = { _, _ -> },
        )
    }
}

@Preview(device = "spec:width=1200dp,height=800dp,dpi=160")
@Composable
private fun OpportunityDetailDesktopPreview() {
    AppTheme {
        OpportunityDetailScreen(
            uiState = previewOpportunityDetailState,
            onUiEvent = {},
            onNavigateBack = {},
            onPreviewAction = {},
            onEditProfile = { _, _ -> },
        )
    }
}

@Preview(widthDp = 320, heightDp = 640, fontScale = 2f)
@Composable
private fun OpportunityDetailAccessibilityPreview() {
    AppTheme {
        OpportunityDetailScreen(
            uiState = previewOpportunityDetailState,
            onUiEvent = {},
            onNavigateBack = {},
            onPreviewAction = {},
            onEditProfile = { _, _ -> },
        )
    }
}

private val previewOpportunityDetailState = OpportunityDetailUiState(
    id = "aurora",
    title = "Aurora Scholars",
    provider = "Northstar Foundation",
    state = EligibilityState.HAS_GAPS,
    stateLabel = "NEEDS ACTION",
    conclusionSummary = "At least one published check needs a change in your profile.",
    timingLabel = "Timing: published deadline 2027-06-01",
    deadlineLabel = "Deadline 2027-06-01",
    checkedLabel = "Checked 2026-10-20",
    sourceUrl = "https://example.invalid/porta-sample/aurora",
    sourceSection = "Eligibility",
    disclaimer = "Sample data only.",
    rules = listOf(
        RuleReceiptUiState(
            id = "ielts",
            result = RuleResult.FAIL,
            explanation = "The synthetic sample requires IELTS 7.0 or above.",
            actualLabel = "6.5 band",
            expectedLabel = "7.0 band",
            sourceNote = "Synthetic sample rule; not a real requirement.",
        ),
    ),
    softCriteria = listOf("Community leadership", "Clear study goals"),
    relatedActionId = "raise-ielts",
)
