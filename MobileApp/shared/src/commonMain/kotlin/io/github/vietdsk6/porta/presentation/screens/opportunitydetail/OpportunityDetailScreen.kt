package io.github.vietdsk6.porta.presentation.screens.opportunitydetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.vietdsk6.porta.designsystem.components.ScreenWithToolbar
import io.github.vietdsk6.porta.designsystem.generated.resources.UiRes
import io.github.vietdsk6.porta.designsystem.generated.resources.ic_back
import io.github.vietdsk6.porta.designsystem.theme.AppTheme
import io.github.vietdsk6.porta.domain.model.RuleResult

@Composable
fun OpportunityDetailScreen(
    viewModel: OpportunityDetailViewModel,
    onNavigateBack: () -> Unit,
    onPreviewAction: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    OpportunityDetailScreen(
        uiState = uiState,
        onUiEvent = viewModel::onUiEvent,
        onNavigateBack = onNavigateBack,
        onPreviewAction = onPreviewAction,
        modifier = modifier.fillMaxSize(),
    )
}

@Composable
fun OpportunityDetailScreen(
    uiState: OpportunityDetailUiState,
    onUiEvent: (OpportunityDetailUiEvent) -> Unit,
    onNavigateBack: () -> Unit,
    onPreviewAction: (String) -> Unit,
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
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Opportunity unavailable", style = AppTheme.typography.h5)
                Text(uiState.error, color = AppTheme.colors.text.secondary)
                OutlinedButton(onClick = onNavigateBack) { Text("Go back") }
            }
            return@ScreenWithToolbar
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize().testTag("opportunity-detail-content"),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            item { OpportunityHeader(uiState) }
            item { CurrentConclusion(uiState) }
            item {
                RuleSection(
                    title = "Needs action",
                    emptyMessage = "No current profile changes are required by the checks Porta can evaluate.",
                    rules = uiState.rules.filter { it.result == RuleResult.FAIL },
                    accent = PORTA_AMBER,
                )
            }
            uiState.relatedActionId?.let { actionId ->
                item {
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            onUiEvent(OpportunityDetailUiEvent.Acknowledged)
                            onPreviewAction(actionId)
                        },
                    ) {
                        Text("Preview this profile change")
                    }
                }
            }
            item {
                RuleSection(
                    title = "Needs verification",
                    emptyMessage = "No checks currently need more information.",
                    rules = uiState.rules.filter { it.result in setOf(RuleResult.UNKNOWN, RuleResult.REVIEW) },
                    accent = AppTheme.colors.primary,
                )
            }
            item {
                RuleSection(
                    title = "Passed checks",
                    emptyMessage = "No encoded checks have passed yet.",
                    rules = uiState.rules.filter { it.result == RuleResult.PASS },
                    accent = PORTA_TEAL,
                )
            }
            item { SoftSelectionFactors(uiState.softCriteria) }
            item { SourceDetails(uiState) }
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
            Text("Current conclusion", style = AppTheme.typography.h6)
            Text(
                uiState.stateLabel,
                color = uiState.stateColor(),
                fontWeight = FontWeight.Bold,
                style = AppTheme.typography.bodySmall,
            )
            Text(uiState.conclusionSummary, color = AppTheme.colors.text.primary)
        }
    }
}

@Composable
private fun RuleSection(
    title: String,
    emptyMessage: String,
    rules: List<RuleReceiptUiState>,
    accent: Color,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(title, style = AppTheme.typography.h6, color = AppTheme.colors.text.primary)
        if (rules.isEmpty()) {
            Text(emptyMessage, color = AppTheme.colors.text.secondary, style = AppTheme.typography.bodyMedium)
        } else {
            rules.forEachIndexed { index, rule ->
                RuleRow(rule = rule, accent = accent)
                if (index != rules.lastIndex) {
                    HorizontalDivider(color = AppTheme.colors.outline.copy(alpha = 0.3f))
                }
            }
        }
    }
}

@Composable
private fun RuleRow(rule: RuleReceiptUiState, accent: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(rule.explanation, style = AppTheme.typography.bodyLarge, color = AppTheme.colors.text.primary)
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text("Your current value", color = AppTheme.colors.text.secondary, style = AppTheme.typography.bodySmall)
            Text(rule.actualLabel, color = accent, fontWeight = FontWeight.SemiBold)
        }
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text("Published check", color = AppTheme.colors.text.secondary, style = AppTheme.typography.bodySmall)
            Text(rule.expectedLabel, fontWeight = FontWeight.SemiBold)
        }
        if (rule.sourceNote.isNotBlank()) {
            Text(rule.sourceNote, color = AppTheme.colors.text.secondary, style = AppTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun SoftSelectionFactors(criteria: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Soft selection factors", style = AppTheme.typography.h6)
        Text(
            "These may affect selection, but Porta never treats them as pass/fail rules.",
            color = AppTheme.colors.text.secondary,
            style = AppTheme.typography.bodyMedium,
        )
        if (criteria.isEmpty()) {
            Text("None listed in the current source pack.", color = AppTheme.colors.text.secondary)
        } else {
            criteria.forEach { criterion -> Text("• $criterion", color = AppTheme.colors.text.primary) }
        }
    }
}

@Composable
private fun SourceDetails(uiState: OpportunityDetailUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Source and last review", style = AppTheme.typography.h6)
        if (uiState.sourceSection.isNotBlank()) {
            Text("Section: ${uiState.sourceSection}", color = AppTheme.colors.text.primary)
        }
        Text(uiState.checkedLabel, color = AppTheme.colors.text.primary)
        Text(uiState.sourceUrl, color = AppTheme.colors.primary, style = AppTheme.typography.bodySmall)
        Text(uiState.disclaimer, color = AppTheme.colors.text.secondary, style = AppTheme.typography.bodyMedium)
    }
}

@Composable
private fun OpportunityDetailUiState.stateColor(): Color = when (stateLabel) {
    "CURRENT CHECKS PASSED" -> PORTA_TEAL
    "NEEDS ACTION" -> PORTA_AMBER
    "NEEDS VERIFICATION" -> AppTheme.colors.primary
    else -> PORTA_CORAL
}

private val PORTA_TEAL = Color(0xFF167D73)
private val PORTA_AMBER = Color(0xFFA66B1F)
private val PORTA_CORAL = Color(0xFFB84F5B)

@Preview
@Composable
private fun OpportunityDetailScreenPreview() {
    AppTheme(isDarkMode = true) {
        OpportunityDetailScreen(
            uiState = OpportunityDetailUiState(
                id = "aurora",
                title = "Aurora Scholars",
                provider = "Northstar Foundation",
                stateLabel = "NEEDS ACTION",
                conclusionSummary = "At least one published check needs a change in your profile.",
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
            ),
            onUiEvent = {},
            onNavigateBack = {},
            onPreviewAction = {},
        )
    }
}
