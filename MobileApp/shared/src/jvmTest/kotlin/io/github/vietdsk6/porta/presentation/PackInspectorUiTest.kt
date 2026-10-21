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

        onNodeWithText("ACTIVE · SYNTHETIC DEMO").assertExists()
        onNodeWithText("ATOMIC · IELTS SCORE ≥ 7.0 band").performScrollTo().assertExists()
        onNodeWithTag("pack-inspector-opportunity-forge").performScrollTo().performClick()
        assertEquals(PackInspectorUiEvent.OpportunitySelected("forge"), events.last())

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
            id = "porta-synthetic-demo",
            name = "Porta synthetic demo pack",
            publisher = "Porta",
            versionLabel = "Version 1",
            schemaLabel = "Schema 1",
            recordCountLabel = "5 records",
            checkedLabel = "Pack checked 2026-10-20",
            verificationRangeLabel = "Records checked 2026-10-20",
            importedLabel = "Imported Oct 20, 2026",
            sourceUrl = "https://example.invalid/porta-sample",
            isBundledDemo = true,
            providers = listOf("Northstar Foundation", "Open Learning Lab"),
            opportunities = listOf(
                PackOpportunityUiState(
                    id = "aurora",
                    title = "Aurora Scholars",
                    provider = "Northstar Foundation",
                    categoryLabel = "Scholarship",
                    checkedLabel = "Checked 2026-10-20",
                    sourceUrl = "https://example.invalid/porta-sample/aurora",
                    sourceSection = "Synthetic eligibility example",
                    ruleNodes = listOf(
                        RuleNodeUiState(
                            id = "aurora-ielts",
                            depth = 0,
                            typeLabel = "ATOMIC · IELTS SCORE ≥ 7.0 band",
                            summary = "The sample requires IELTS 7.0 or above.",
                            sourceNote = "Synthetic fixture.",
                        ),
                    ),
                ),
                PackOpportunityUiState(
                    id = "forge",
                    title = "Forge Student Lab",
                    provider = "Open Learning Lab",
                    categoryLabel = "Internship",
                    checkedLabel = "Checked 2026-10-20",
                    sourceUrl = "https://example.invalid/porta-sample/forge",
                    sourceSection = "Synthetic eligibility example",
                    ruleNodes = emptyList(),
                ),
            ),
        ),
        selectedOpportunityId = "aurora",
    )
}
