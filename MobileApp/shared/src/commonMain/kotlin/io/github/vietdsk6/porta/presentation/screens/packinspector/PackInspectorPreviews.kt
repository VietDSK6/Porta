package io.github.vietdsk6.porta.presentation.screens.packinspector

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import io.github.vietdsk6.porta.designsystem.theme.AppTheme
import io.github.vinceglb.filekit.name

@Preview
@Composable
private fun PackInspectorScreenPreview() {
    AppTheme {
        PackInspectorScreen(
            uiState = previewState,
            onUiEvent = {},
            onNavigateBack = {},
            onImportPack = {},
            onOpenSource = {},
        )
    }
}

@Preview(device = "spec:width=1200dp,height=800dp,dpi=160")
@Composable
private fun PackInspectorDesktopPreview() {
    AppTheme {
        PackInspectorScreen(
            uiState = previewState,
            onUiEvent = {},
            onNavigateBack = {},
            onImportPack = {},
            onOpenSource = {},
        )
    }
}

private val previewState = PackInspectorUiState(
    isLoading = false,
    activePack = ActivePackUiState(
        id = "porta-curated-opportunities-2026-10-21",
        name = "Porta curated opportunity pack",
        publisher = "Porta · independently curated from official sources",
        versionLabel = "Version 1",
        schemaLabel = "Schema 1",
        recordCountLabel = "15 records",
        checkedLabel = "Pack checked 2026-10-21",
        sourceReviewRangeLabel = "Source records reviewed on 2026-10-21",
        importedLabel = "Imported Oct 21, 2026",
        sourceUrl = null,
        isBundledPack = true,
        providers = listOf("CERN openlab", "Kotlin Foundation", "UNICEF"),
        opportunities = listOf(
            PackOpportunityUiState(
                id = "kotlin-student-coding-competition-2026",
                title = "Kotlin Student Coding Competition",
                provider = "Kotlin Foundation",
                categoryLabel = "Student contest",
                checkedLabel = "Checked 2026-10-21",
                sourceUrl = "https://kotlinconf.com/contest-rules/",
                sourceSection = "Eligibility; How to Enter",
                ruleNodes = listOf(
                    RuleNodeUiState(
                        id = "kotlin-root",
                        depth = 0,
                        typeLabel = "ALL",
                        summary = "Every one of 3 child rules must match.",
                    ),
                ),
            ),
            PackOpportunityUiState(
                id = "unicef-internship-rolling",
                title = "UNICEF Internship Programme",
                provider = "UNICEF",
                categoryLabel = "Internship",
                checkedLabel = "Checked 2026-10-21",
                sourceUrl = "https://www.unicef.org/careers/internships",
                sourceSection = "Requirements; How to apply",
                ruleNodes = emptyList(),
            ),
        ),
    ),
    selectedOpportunityId = "kotlin-student-coding-competition-2026",
)
