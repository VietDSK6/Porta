package io.github.vietdsk6.porta.presentation

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.runComposeUiTest
import io.github.vietdsk6.porta.designsystem.theme.AppTheme
import io.github.vietdsk6.porta.presentation.screens.packinspector.ActivePackUiState
import io.github.vietdsk6.porta.presentation.screens.packinspector.PackDiagnosticSeverityUi
import io.github.vietdsk6.porta.presentation.screens.packinspector.PackDiagnosticUiState
import io.github.vietdsk6.porta.presentation.screens.packinspector.PackInspectorScreen
import io.github.vietdsk6.porta.presentation.screens.packinspector.PackInspectorUiEvent
import io.github.vietdsk6.porta.presentation.screens.packinspector.PackInspectorUiState
import io.github.vietdsk6.porta.presentation.screens.packinspector.PackOpportunityUiState
import io.github.vietdsk6.porta.presentation.screens.packinspector.RuleNodeUiState
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class PackInspectorUiTest {
    @Test
    fun `active pack exposes provenance rules and pack controls`() = runComposeUiTest {
        val events = mutableListOf<PackInspectorUiEvent>()
        setContent {
            AppTheme {
                PackInspectorScreen(
                    uiState = activePackState,
                    onUiEvent = { events += it },
                    onNavigateBack = {},
                    onImportPack = {},
                    onOpenSource = {},
                )
            }
        }

        onNodeWithText("ACTIVE · CURATED BUNDLE").assertExists()
        onNodeWithText("ATOMIC · AGE ≥ 18.0 years").performScrollTo().assertExists()
        onNodeWithTag("pack-inspector-opportunity-unicef-internship-rolling").performScrollTo().performClick()
        assertEquals(PackInspectorUiEvent.OpportunitySelected("unicef-internship-rolling"), events.last())

        onNodeWithTag("pack-inspector-export").performScrollTo().performClick()
        assertEquals(PackInspectorUiEvent.ExportActivePack, events.last())
    }

    @Test
    fun `rejected import keeps diagnostics actionable`() = runComposeUiTest {
        val events = mutableListOf<PackInspectorUiEvent>()
        setContent {
            AppTheme {
                PackInspectorScreen(
                    uiState = activePackState.copy(
                        diagnostics = listOf(
                            PackDiagnosticUiState(
                                code = "missing_source_note",
                                path = "opportunities[0].eligibilityRule.sourceNote",
                                message = "Hard rules require a source note.",
                                severity = PackDiagnosticSeverityUi.ERROR,
                            ),
                        ),
                    ),
                    onUiEvent = { events += it },
                    onNavigateBack = {},
                    onImportPack = {},
                    onOpenSource = {},
                )
            }
        }

        onNodeWithText("Import diagnostics · 1").performScrollTo().assertExists()
        onNodeWithText("Hard rules require a source note.").assertExists()
        onNodeWithTag("pack-inspector-export-diagnostics").performScrollTo().performClick()
        assertEquals(PackInspectorUiEvent.ExportDiagnostics, events.last())
    }

    private val activePackState = PackInspectorUiState(
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
            providers = listOf("Kotlin Foundation", "UNICEF"),
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
                            id = "kotlin-age",
                            depth = 0,
                            typeLabel = "ATOMIC · AGE ≥ 18.0 years",
                            summary = "Entrants must be at least 18.",
                            sourceNote = "Official rules checked 2026-10-21.",
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
}
