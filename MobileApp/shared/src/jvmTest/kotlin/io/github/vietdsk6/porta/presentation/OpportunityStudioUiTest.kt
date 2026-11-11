package io.github.vietdsk6.porta.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import io.github.vietdsk6.porta.designsystem.theme.AppTheme
import io.github.vietdsk6.porta.domain.model.Actionability
import io.github.vietdsk6.porta.domain.model.DraftGroundingStatus
import io.github.vietdsk6.porta.domain.model.DraftRuleKind
import io.github.vietdsk6.porta.domain.model.FactKey
import io.github.vietdsk6.porta.domain.model.Operator
import io.github.vietdsk6.porta.domain.model.OpportunityStudioDraft
import io.github.vietdsk6.porta.domain.model.OpportunityStudioExpectedValueDraft
import io.github.vietdsk6.porta.domain.model.OpportunityStudioRuleDraft
import io.github.vietdsk6.porta.domain.model.RuleDraftSuggestion
import io.github.vietdsk6.porta.domain.model.SourceTextRange
import io.github.vietdsk6.porta.domain.model.StudioFactValueKind
import io.github.vietdsk6.porta.presentation.screens.opportunitystudio.OpportunityStudioDiagnosticUiState
import io.github.vietdsk6.porta.presentation.screens.opportunitystudio.OpportunityStudioMetadataField
import io.github.vietdsk6.porta.presentation.screens.opportunitystudio.OpportunityStudioScreen
import io.github.vietdsk6.porta.presentation.screens.opportunitystudio.OpportunityStudioUiEvent
import io.github.vietdsk6.porta.presentation.screens.opportunitystudio.OpportunityStudioUiState
import io.github.vietdsk6.porta.presentation.screens.opportunitystudio.opportunityStudioUsesTwoPane
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class OpportunityStudioUiTest {
    @Test
    fun `unsupported platform shows authoring boundary`() = runComposeUiTest {
        setContent {
            AppTheme {
                OpportunityStudioScreen(
                    uiState = OpportunityStudioUiState(isLoading = false, isSupported = false),
                    onUiEvent = {},
                    onNavigateBack = {},
                )
            }
        }

        onNodeWithTag("opportunity-studio-unsupported").assertExists()
        onNodeWithText("Opportunity Studio is unavailable on this device", substring = true).assertExists()
        onNodeWithTag("opportunity-studio-export").assertDoesNotExist()
    }

    @Test
    fun `metadata and rule controls emit typed events`() = runComposeUiTest {
        val events = mutableListOf<OpportunityStudioUiEvent>()
        setContent {
            AppTheme {
                OpportunityStudioScreen(
                    uiState = invalidState(),
                    onUiEvent = { events += it },
                    onNavigateBack = {},
                )
            }
        }

        onNodeWithTag("opportunity-studio-title").performTextReplacement("New title")
        assertEquals(
            OpportunityStudioUiEvent.MetadataChanged(
                OpportunityStudioMetadataField.TITLE,
                "New title",
            ),
            events.last(),
        )
        onNodeWithTag("opportunity-studio-add-manual").performScrollTo().performClick()
        assertEquals(OpportunityStudioUiEvent.AddManualRule, events.last())
        onNodeWithTag("opportunity-studio-root-any").performScrollTo().performClick()
        assertIs<OpportunityStudioUiEvent.RootRuleKindChanged>(events.last())
    }

    @Test
    fun `validation blocks invalid export and exposes valid JSON preview`() = runComposeUiTest {
        var state by mutableStateOf(invalidState())
        setContent {
            AppTheme {
                OpportunityStudioScreen(
                    uiState = state,
                    onUiEvent = {},
                    onNavigateBack = {},
                )
            }
        }

        onNodeWithTag("opportunity-studio-invalid").performScrollTo().assertExists()
        onNodeWithTag("opportunity-studio-export").performScrollTo().assertIsNotEnabled()

        state = state.copy(
            diagnostics = emptyList(),
            rawJson = "{\"schemaVersion\":2}",
            isJsonPreviewVisible = true,
        )
        waitForIdle()
        onNodeWithTag("opportunity-studio-valid").performScrollTo().assertExists()
        onNodeWithTag("opportunity-studio-json-preview").performScrollTo().assertExists()
        onNodeWithTag("opportunity-studio-export").performScrollTo().assertIsEnabled()
    }

    @Test
    fun `AI suggestions show grounded quote and block unsupported quote acceptance`() = runComposeUiTest {
        val events = mutableListOf<OpportunityStudioUiEvent>()
        val source = "Applicants must be at least 18 years old."
        setContent {
            AppTheme {
                OpportunityStudioScreen(
                    uiState = invalidState().copy(
                        pastedSourceText = source,
                        aiSuggestions = listOf(
                            RuleDraftSuggestion(
                                id = "ai-1",
                                kind = DraftRuleKind.ATOMIC,
                                factKey = FactKey.AGE,
                                operator = Operator.GREATER_THAN_OR_EQUAL,
                                quote = "at least 18 years old",
                                rationale = "Age starts at 18.",
                                groundingStatus = DraftGroundingStatus.GROUNDED,
                                sourceRange = SourceTextRange(19, 40),
                            ),
                            RuleDraftSuggestion(
                                id = "ai-2",
                                kind = DraftRuleKind.MANUAL_CHECK,
                                quote = "a guaranteed award",
                                rationale = "Verify the award rule.",
                                groundingStatus = DraftGroundingStatus.REJECTED_UNGROUNDED,
                            ),
                        ),
                    ),
                    onUiEvent = { events += it },
                    onNavigateBack = {},
                )
            }
        }

        onNodeWithTag("opportunity-studio-ai-trust").assertExists()
        onNodeWithTag("opportunity-studio-ai-quote-ai-1").performScrollTo().assertExists()
        onNodeWithTag("opportunity-studio-ai-status-ai-2").performScrollTo().assertExists()
        onNodeWithTag("opportunity-studio-ai-accept-ai-2").performScrollTo().assertIsNotEnabled()
        onNodeWithTag("opportunity-studio-ai-accept-ai-1").performScrollTo().performClick()
        assertEquals(OpportunityStudioUiEvent.SuggestionAccepted("ai-1"), events.last())
        onNodeWithTag("opportunity-studio-ai-reject-ai-2").performScrollTo().performClick()
        assertEquals(OpportunityStudioUiEvent.SuggestionRejected("ai-2"), events.last())
    }

    @Test
    fun `offline demo source selection sends official text and URL`() = runComposeUiTest {
        val events = mutableListOf<OpportunityStudioUiEvent>()
        setContent {
            AppTheme {
                OpportunityStudioScreen(
                    uiState = invalidState(),
                    onUiEvent = { events += it },
                    onNavigateBack = {},
                )
            }
        }
        onNodeWithTag("opportunity-studio-ai-demo-0").performScrollTo().performClick()
        val event = assertIs<OpportunityStudioUiEvent.DemoSourceSelected>(events.last())
        assertTrue(event.sourceText.isNotBlank())
        assertTrue(event.sourceUrl.startsWith("https://"))
    }

    @Test
    fun `layout uses two panes only for wide standard text`() {
        assertTrue(opportunityStudioUsesTwoPane(maxWidth = 1280.dp, fontScale = 1f))
        assertFalse(opportunityStudioUsesTwoPane(maxWidth = 800.dp, fontScale = 1f))
        assertFalse(opportunityStudioUsesTwoPane(maxWidth = 1280.dp, fontScale = 2f))
    }

    private fun invalidState(): OpportunityStudioUiState = OpportunityStudioUiState(
        isLoading = false,
        basePackName = "Porta curated opportunities",
        basePackVersion = 3,
        nextPackVersion = 4,
        draft = OpportunityStudioDraft(
            opportunityId = "new-opportunity",
            title = "",
            provider = "Example provider",
            cycle = "2027",
            sourceUrl = "https://example.edu/opportunity",
            lastCheckedOn = "2026-11-04",
            rules = listOf(
                OpportunityStudioRuleDraft.Atomic(
                    id = "enrolled",
                    factKey = FactKey.ENROLLED,
                    operator = Operator.EQUALS,
                    expected = OpportunityStudioExpectedValueDraft(
                        kind = StudioFactValueKind.BOOLEAN,
                        value = "true",
                    ),
                    explanation = "Applicants must be enrolled.",
                    sourceNote = "Eligibility",
                    actionability = Actionability.CONTEXT_ONLY,
                ),
            ),
        ),
        selectedRuleIndex = 0,
        diagnostics = listOf(
            OpportunityStudioDiagnosticUiState(
                path = "opportunities[15].title",
                message = "Title is required.",
            ),
        ),
    )
}
