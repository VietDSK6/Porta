package io.github.vietdsk6.porta.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import io.github.vietdsk6.porta.designsystem.theme.AppTheme
import io.github.vietdsk6.porta.domain.model.EligibilityState
import io.github.vietdsk6.porta.domain.model.RuleResult
import io.github.vietdsk6.porta.presentation.screens.opportunitydetail.OpportunityDetailScreen
import io.github.vietdsk6.porta.presentation.screens.opportunitydetail.OpportunityDetailUiState
import io.github.vietdsk6.porta.presentation.screens.opportunitydetail.RuleReceiptUiState
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class OpportunityDetailProgressiveDisclosureUiTest {
    @Test
    fun `rule evidence and source provenance are disclosed on demand`() = runComposeUiTest {
        setContent {
            AppTheme {
                Box(Modifier.size(width = 390.dp, height = 800.dp)) {
                    OpportunityDetailScreen(
                        uiState = detailState,
                        onUiEvent = {},
                        onNavigateBack = {},
                        onPreviewAction = {},
                    )
                }
            }
        }

        onNodeWithText("6.5 band").assertExists()
        onNodeWithText("7.0 band").assertExists()
        onNodeWithText("Synthetic evidence note").assertDoesNotExist()
        onNodeWithText("Eligibility section").assertDoesNotExist()

        onNodeWithTag("opportunity-evidence-toggle").performScrollTo().performClick()
        onNodeWithText("Synthetic evidence note", substring = true).assertExists()

        onNodeWithTag("opportunity-detail-content").performScrollToIndex(7)
        onNodeWithTag("opportunity-source-toggle").performScrollTo().performClick()
        onNodeWithText("Section: Eligibility section").assertExists()
    }

    @Test
    fun `empty receipt sections do not add audit report copy`() = runComposeUiTest {
        setContent {
            AppTheme {
                Box(Modifier.size(width = 390.dp, height = 800.dp)) {
                    OpportunityDetailScreen(
                        uiState = detailState.copy(rules = emptyList(), softCriteria = emptyList()),
                        onUiEvent = {},
                        onNavigateBack = {},
                        onPreviewAction = {},
                    )
                }
            }
        }

        onNodeWithText("No current profile changes are required", substring = true).assertDoesNotExist()
        onNodeWithText("No checks currently need more information", substring = true).assertDoesNotExist()
        onNodeWithText("None listed in the current source pack").assertDoesNotExist()
        onNodeWithText("Evidence behind this conclusion").assertDoesNotExist()
    }

    private val detailState = OpportunityDetailUiState(
        id = "aurora",
        title = "Aurora Scholars",
        provider = "Northstar Foundation",
        state = EligibilityState.HAS_GAPS,
        stateLabel = "NEEDS ACTION",
        conclusionSummary = "One published check needs a profile change.",
        timingLabel = "Deadline 2027-06-01",
        deadlineLabel = "Deadline 2027-06-01",
        checkedLabel = "Checked 2026-10-20",
        sourceUrl = "https://example.com/aurora",
        sourceSection = "Eligibility section",
        disclaimer = "Sample data only.",
        rules = listOf(
            RuleReceiptUiState(
                id = "ielts",
                result = RuleResult.FAIL,
                explanation = "Requires IELTS 7.0 or above.",
                actualLabel = "6.5 band",
                expectedLabel = "7.0 band",
                sourceNote = "Synthetic evidence note",
            ),
        ),
        softCriteria = listOf("Community leadership"),
        nextStepGuidance = "Preview a measurable language milestone.",
        isSampleProfile = false,
        isSampleCatalog = false,
    )
}
