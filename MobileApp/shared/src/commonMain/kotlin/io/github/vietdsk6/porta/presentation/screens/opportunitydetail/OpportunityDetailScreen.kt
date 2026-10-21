package io.github.vietdsk6.porta.presentation.screens.opportunitydetail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
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
        title = "Eligibility receipt",
        includeBottomInsets = true,
        navigationIcon = UiRes.drawable.ic_back,
        onNavigationIconClick = onNavigateBack,
    ) {
        if (uiState.error != null) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Receipt unavailable", style = AppTheme.typography.h5)
                Text(uiState.error, color = AppTheme.colors.text.secondary)
                OutlinedButton(onClick = onNavigateBack) { Text("Go back") }
            }
            return@ScreenWithToolbar
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item { ReceiptHeader(uiState) }
            item {
                Text(
                    "Encoded hard rules",
                    style = AppTheme.typography.h6,
                    color = AppTheme.colors.text.primary,
                )
            }
            items(uiState.rules, key = RuleReceiptUiState::id) { rule -> RuleReceiptCard(rule) }
            item { SourceCard(uiState) }
            uiState.relatedActionId?.let { actionId ->
                item {
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            onUiEvent(OpportunityDetailUiEvent.Acknowledged)
                            onPreviewAction(actionId)
                        },
                    ) {
                        Text("Preview the related milestone")
                    }
                }
            }
        }
    }
}

@Composable
private fun ReceiptHeader(uiState: OpportunityDetailUiState) {
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
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(uiState.stateLabel, color = PORTA_AMBER, fontWeight = FontWeight.Bold)
            Text(uiState.deadlineLabel, color = AppTheme.colors.text.secondary)
        }
    }
}

@Composable
private fun RuleReceiptCard(rule: RuleReceiptUiState) {
    val accent = rule.result.color()
    Card(
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surfaceContainer),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.7f)),
        shape = RoundedCornerShape(18.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(rule.result.name, color = accent, fontWeight = FontWeight.Bold)
            Text(rule.explanation, style = AppTheme.typography.bodyLarge)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ReceiptValue("Actual", rule.actualLabel, Modifier.weight(1f))
                ReceiptValue("Required", rule.expectedLabel, Modifier.weight(1f))
            }
            Text(rule.sourceNote, color = AppTheme.colors.text.secondary, style = AppTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun ReceiptValue(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(label, color = AppTheme.colors.text.secondary, style = AppTheme.typography.bodySmall)
        Text(value, fontWeight = FontWeight.SemiBold, style = AppTheme.typography.bodyLarge)
    }
}

@Composable
private fun SourceCard(uiState: OpportunityDetailUiState) {
    Card(
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.alternative),
        shape = RoundedCornerShape(18.dp),
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Source & limits", color = AppTheme.colors.onAlternative, fontWeight = FontWeight.Bold)
            Text(uiState.checkedLabel, color = AppTheme.colors.text.primary)
            Text(uiState.sourceUrl, color = AppTheme.colors.primary, style = AppTheme.typography.bodySmall)
            Text(uiState.disclaimer, color = AppTheme.colors.text.secondary, style = AppTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun RuleResult.color(): Color = when (this) {
    RuleResult.PASS -> PORTA_TEAL

    RuleResult.FAIL -> PORTA_AMBER

    RuleResult.UNKNOWN,
    RuleResult.REVIEW,
    -> AppTheme.colors.primary
}

private val PORTA_TEAL = Color(0xFF2DD4BF)
private val PORTA_AMBER = Color(0xFFF4B860)

@Preview
@Composable
private fun OpportunityDetailScreenPreview() {
    AppTheme(isDarkMode = true) {
        OpportunityDetailScreen(
            uiState = OpportunityDetailUiState(
                id = "aurora",
                title = "Aurora Scholars",
                provider = "Northstar Foundation",
                stateLabel = "HAS GAPS",
                deadlineLabel = "Deadline 2027-06-01",
                checkedLabel = "Checked 2026-10-20",
                sourceUrl = "https://example.invalid/porta-sample/aurora",
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
                relatedActionId = "raise-ielts",
            ),
            onUiEvent = {},
            onNavigateBack = {},
            onPreviewAction = {},
        )
    }
}
