package io.github.vietdsk6.porta.presentation.screens.opportunitystudio

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import io.github.vietdsk6.porta.designsystem.theme.AppTheme
import io.github.vietdsk6.porta.domain.model.Actionability
import io.github.vietdsk6.porta.domain.model.FactKey
import io.github.vietdsk6.porta.domain.model.Operator
import io.github.vietdsk6.porta.domain.model.OpportunityCategory
import io.github.vietdsk6.porta.domain.model.OpportunityStudioDraft
import io.github.vietdsk6.porta.domain.model.OpportunityStudioExpectedValueDraft
import io.github.vietdsk6.porta.domain.model.OpportunityStudioRuleDraft
import io.github.vietdsk6.porta.domain.model.StudioDeadlineKind
import io.github.vietdsk6.porta.domain.model.StudioFactValueKind

@Preview(device = "spec:width=1280dp,height=1000dp,dpi=160")
@Composable
internal fun OpportunityStudioDesktopPreview() {
    AppTheme {
        OpportunityStudioScreen(
            uiState = previewStudioState(),
            onUiEvent = {},
            onNavigateBack = {},
        )
    }
}

@Preview(widthDp = 700, heightDp = 1000, fontScale = 2f)
@Composable
internal fun OpportunityStudioLargeTextPreview() {
    AppTheme {
        OpportunityStudioScreen(
            uiState = previewStudioState(),
            onUiEvent = {},
            onNavigateBack = {},
        )
    }
}

internal fun previewStudioState(): OpportunityStudioUiState = OpportunityStudioUiState(
    isLoading = false,
    basePackName = "Porta curated opportunities",
    basePackVersion = 3,
    nextPackVersion = 4,
    draft = OpportunityStudioDraft(
        opportunityId = "sample-scholarship-2027",
        title = "Sample source-linked scholarship",
        provider = "Example University",
        category = OpportunityCategory.SCHOLARSHIP,
        cycle = "2027",
        sourceUrl = "https://example.edu/scholarship",
        sourceSection = "Eligibility",
        lastCheckedOn = "2026-11-04",
        deadlineKind = StudioDeadlineKind.KNOWN,
        deadlineDate = "2027-03-01",
        rules = listOf(
            OpportunityStudioRuleDraft.Atomic(
                id = "ielts-minimum",
                factKey = FactKey.IELTS_SCORE,
                operator = Operator.GREATER_THAN_OR_EQUAL,
                expected = OpportunityStudioExpectedValueDraft(
                    kind = StudioFactValueKind.NUMBER,
                    value = "7.0",
                    unit = "band",
                ),
                explanation = "Applicants need an IELTS score of at least 7.0.",
                sourceNote = "Eligibility · English language requirement",
                actionability = Actionability.CONTROLLABLE,
            ),
            OpportunityStudioRuleDraft.ManualCheck(
                id = "academic-fit",
                explanation = "Review the published academic-fit guidance.",
            ),
        ),
    ),
    selectedRuleIndex = 0,
    diagnostics = listOf(
        OpportunityStudioDiagnosticUiState(
            path = "opportunities[15].eligibilityRule.children[1].sourceNote",
            message = "Add a source note before export.",
        ),
    ),
)
