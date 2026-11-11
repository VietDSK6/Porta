package io.github.vietdsk6.porta.presentation.screens.opportunitystudio

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import io.github.vietdsk6.porta.data.source.remote.draft.DraftDemoFixtures
import io.github.vietdsk6.porta.designsystem.theme.AppTheme
import io.github.vietdsk6.porta.domain.model.DraftGroundingStatus
import io.github.vietdsk6.porta.domain.model.DraftReviewStatus
import io.github.vietdsk6.porta.domain.model.DraftRuleKind
import io.github.vietdsk6.porta.domain.model.RuleDraftSuggestion
import io.github.vietdsk6.porta.domain.model.SourceTextRange

private const val TRUST_BANNER =
    "AI drafts only. Every rule must quote the source verbatim and be approved by you. " +
        "Porta never uses AI to decide eligibility."

@Composable
fun AiDraftPanel(
    uiState: OpportunityStudioUiState,
    onUiEvent: (OpportunityStudioUiEvent) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("opportunity-studio-ai-panel"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surfaceContainer),
        border = BorderStroke(1.dp, AppTheme.colors.outline.copy(alpha = 0.4f)),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(AppTheme.spacing.cardContentSpacing),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Draft sourced rules", style = AppTheme.typography.h5, fontWeight = FontWeight.Bold)
            Surface(
                color = AppTheme.colors.alternative,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, AppTheme.colors.primary.copy(alpha = 0.35f)),
            ) {
                Text(
                    TRUST_BANNER,
                    modifier = Modifier.fillMaxWidth().padding(12.dp).testTag("opportunity-studio-ai-trust"),
                    style = AppTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                )
            }
            Text(
                "Paste the official source passage. The offline demo works without an API key; a key in settings " +
                    "enables Gemini drafting.",
                color = AppTheme.colors.text.secondary,
                style = AppTheme.typography.bodySmall,
            )
            Text("Try a demo source", style = AppTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DraftDemoFixtures.all.forEachIndexed { index, fixture ->
                    OutlinedButton(
                        onClick = {
                            onUiEvent(OpportunityStudioUiEvent.DemoSourceSelected(fixture.sourceText, fixture.sourceUrl))
                        },
                        modifier = Modifier.testTag("opportunity-studio-ai-demo-$index"),
                    ) { Text(fixture.title) }
                }
            }
            OutlinedTextField(
                value = uiState.pastedSourceText,
                onValueChange = { onUiEvent(OpportunityStudioUiEvent.PastedSourceChanged(it)) },
                modifier = Modifier.fillMaxWidth().testTag("opportunity-studio-ai-source"),
                label = { Text("Official source text") },
                minLines = 5,
                maxLines = 12,
            )
            Button(
                onClick = { onUiEvent(OpportunityStudioUiEvent.DraftRulesRequested) },
                enabled = uiState.pastedSourceText.isNotBlank() && !uiState.isDraftingRules,
                modifier = Modifier.testTag("opportunity-studio-ai-draft"),
            ) {
                if (uiState.isDraftingRules) {
                    CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp))
                }
                Text(if (uiState.isDraftingRules) "Drafting…" else "Draft rules")
            }
            uiState.aiDraftMessage?.let { message ->
                Text(message, style = AppTheme.typography.bodySmall, color = AppTheme.colors.text.secondary)
            }
            uiState.aiSuggestions.forEach { suggestion ->
                SuggestionCard(
                    suggestion = suggestion,
                    sourceText = uiState.pastedSourceText,
                    isEditing = uiState.editingSuggestionId == suggestion.id,
                    onUiEvent = onUiEvent,
                )
            }
        }
    }
}

@Composable
private fun SuggestionCard(
    suggestion: RuleDraftSuggestion,
    sourceText: String,
    isEditing: Boolean,
    onUiEvent: (OpportunityStudioUiEvent) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("opportunity-studio-ai-suggestion-${suggestion.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.background),
        border = BorderStroke(1.dp, AppTheme.colors.outline.copy(alpha = 0.35f)),
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    if (suggestion.kind == DraftRuleKind.ATOMIC) "ASSESSED RULE" else "MANUAL CHECK",
                    style = AppTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.primary,
                )
                Text(
                    suggestion.statusLabel(),
                    modifier = Modifier.testTag("opportunity-studio-ai-status-${suggestion.id}"),
                    style = AppTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = if (suggestion.groundingStatus == DraftGroundingStatus.GROUNDED) {
                        AppTheme.colors.status.success
                    } else {
                        AppTheme.colors.status.warning
                    },
                )
            }
            if (suggestion.kind == DraftRuleKind.ATOMIC) {
                Text(
                    "${suggestion.factKey?.name.orEmpty()} · ${suggestion.operator?.name.orEmpty()}",
                    style = AppTheme.typography.bodySmall,
                )
            }
            Text(suggestion.rationale, style = AppTheme.typography.bodyMedium)
            suggestion.manualFallbackReason?.let { reason ->
                Text("Needs human interpretation: $reason", style = AppTheme.typography.bodySmall)
            }
            HighlightedSourceQuote(sourceText, suggestion.sourceRange, suggestion.quote, suggestion.id)
            if (isEditing && suggestion.reviewStatus == DraftReviewStatus.PENDING) {
                OutlinedTextField(
                    value = suggestion.quote,
                    onValueChange = { onUiEvent(OpportunityStudioUiEvent.SuggestionQuoteChanged(suggestion.id, it)) },
                    modifier = Modifier.fillMaxWidth().testTag("opportunity-studio-ai-edit-quote"),
                    label = { Text("Quoted source words") },
                    minLines = 2,
                )
                OutlinedTextField(
                    value = suggestion.rationale,
                    onValueChange = { onUiEvent(OpportunityStudioUiEvent.SuggestionRationaleChanged(suggestion.id, it)) },
                    modifier = Modifier.fillMaxWidth().testTag("opportunity-studio-ai-edit-rationale"),
                    label = { Text("Rule explanation") },
                    minLines = 2,
                )
                Text(
                    "After acceptance, review the fact, operator and value in the rule builder below.",
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.colors.text.secondary,
                )
            }
            if (suggestion.reviewStatus == DraftReviewStatus.PENDING) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { onUiEvent(OpportunityStudioUiEvent.SuggestionAccepted(suggestion.id)) },
                        enabled = suggestion.canAccept,
                        modifier = Modifier.testTag("opportunity-studio-ai-accept-${suggestion.id}"),
                    ) { Text("Accept") }
                    OutlinedButton(
                        onClick = { onUiEvent(OpportunityStudioUiEvent.SuggestionEditRequested(suggestion.id)) },
                        modifier = Modifier.testTag("opportunity-studio-ai-edit-${suggestion.id}"),
                    ) { Text(if (isEditing) "Done editing" else "Edit") }
                    TextButton(
                        onClick = { onUiEvent(OpportunityStudioUiEvent.SuggestionRejected(suggestion.id)) },
                        modifier = Modifier.testTag("opportunity-studio-ai-reject-${suggestion.id}"),
                    ) { Text("Reject") }
                }
            }
        }
    }
}

@Composable
private fun HighlightedSourceQuote(
    sourceText: String,
    range: SourceTextRange?,
    quote: String,
    suggestionId: String,
) {
    val start = range?.start ?: -1
    val end = range?.endExclusive ?: -1
    if (start < 0 || end <= start || end > sourceText.length) {
        Text(
            "Quote not found in pasted source: “$quote”",
            modifier = Modifier.testTag("opportunity-studio-ai-quote-$suggestionId"),
            style = AppTheme.typography.bodySmall,
            color = AppTheme.colors.status.warning,
        )
        return
    }
    val excerptStart = (start - 80).coerceAtLeast(0)
    val excerptEnd = (end + 80).coerceAtMost(sourceText.length)
    val highlighted = buildAnnotatedString {
        if (excerptStart > 0) append("…")
        append(sourceText.substring(excerptStart, start))
        withStyle(SpanStyle(background = AppTheme.colors.primary.copy(alpha = 0.25f), fontWeight = FontWeight.Bold)) {
            append(sourceText.substring(start, end))
        }
        append(sourceText.substring(end, excerptEnd))
        if (excerptEnd < sourceText.length) append("…")
    }
    Text(
        text = highlighted,
        modifier = Modifier.fillMaxWidth().testTag("opportunity-studio-ai-quote-$suggestionId"),
        style = AppTheme.typography.bodySmall,
    )
}

private fun RuleDraftSuggestion.statusLabel(): String = when (reviewStatus) {
    DraftReviewStatus.ACCEPTED -> "Accepted · grounded"

    DraftReviewStatus.REJECTED -> "Rejected by curator"

    DraftReviewStatus.PENDING -> when (groundingStatus) {
        DraftGroundingStatus.GROUNDED -> "Grounded · quote found"
        DraftGroundingStatus.REJECTED_UNGROUNDED -> "Rejected: quote not found"
        DraftGroundingStatus.UNCHECKED -> "Checking quote"
    }
}
