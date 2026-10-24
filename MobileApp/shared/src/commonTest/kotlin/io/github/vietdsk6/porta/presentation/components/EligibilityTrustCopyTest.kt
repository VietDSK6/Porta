package io.github.vietdsk6.porta.presentation.components

import io.github.vietdsk6.porta.domain.model.Deadline
import io.github.vietdsk6.porta.domain.model.EligibilityReceipt
import io.github.vietdsk6.porta.domain.model.EligibilityState
import io.github.vietdsk6.porta.domain.model.RuleResult
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class EligibilityTrustCopyTest {
    @Test
    fun `passed rules with unknown timing are presented as two separate conclusions`() {
        val receipt = receipt(
            state = EligibilityState.NEEDS_INFO,
            rootResult = RuleResult.PASS,
            deadline = Deadline.Tba,
        )

        assertEquals("Timing needs confirmation", receipt.headlineLabel())
        assertEquals("Assessed rules passed", receipt.requirementAssessmentLabel())
        assertEquals("Timing: provider date not yet published", receipt.timingLabel())
        assertFalse(receipt.trustSummary().contains("eligible", ignoreCase = true))
        assertFalse(receipt.trustSummary().contains("verified", ignoreCase = true))
    }

    @Test
    fun `open is described as an assessment rather than full eligibility`() {
        val receipt = receipt(
            state = EligibilityState.OPEN,
            rootResult = RuleResult.PASS,
            deadline = Deadline.Known(LocalDate(2027, 6, 1)),
        )

        assertEquals("Assessed rules passed", receipt.headlineLabel())
        assertEquals("Timing: published deadline 2027-06-01", receipt.timingLabel())
        assertFalse(receipt.trustSummary().contains("verified", ignoreCase = true))
    }

    private fun receipt(
        state: EligibilityState,
        rootResult: RuleResult,
        deadline: Deadline,
    ): EligibilityReceipt = EligibilityReceipt(
        opportunityId = "test-opportunity",
        state = state,
        rootResult = rootResult,
        rules = emptyList(),
        deadline = deadline,
        lastCheckedOn = LocalDate(2026, 10, 24),
    )
}
