package io.github.vietdsk6.porta.presentation.components

import io.github.vietdsk6.porta.domain.model.Deadline
import io.github.vietdsk6.porta.domain.model.EligibilityReceipt
import io.github.vietdsk6.porta.domain.model.EligibilityState
import io.github.vietdsk6.porta.domain.model.FactKey
import io.github.vietdsk6.porta.domain.model.OpportunityCycleStatus
import io.github.vietdsk6.porta.domain.model.RuleReceipt
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

    @Test
    fun `display copy separates missing fact from manual source review`() {
        val missing = receipt(EligibilityState.NEEDS_INFO, RuleResult.UNKNOWN, Deadline.Tba).copy(
            rules = listOf(RuleReceipt("enrollment", RuleResult.UNKNOWN, "Original source wording", FactKey.ENROLLED)),
        )
        val manual = receipt(EligibilityState.NEEDS_INFO, RuleResult.REVIEW, Deadline.Tba).copy(
            rules = listOf(RuleReceipt("manual", RuleResult.REVIEW, "Original source wording")),
        )

        assertEquals("Add or confirm enrolled.", missing.displayCopy().shortReason)
        assertEquals("Confirm a condition with the official source.", manual.displayCopy().shortReason)
        assertEquals("Needs source review", manual.displayCopy().requirementLabel)
        assertEquals("Original source wording", manual.rules.single().explanation)
    }

    @Test
    fun `display copy keeps known and unpublished timing distinct`() {
        val known = receipt(EligibilityState.OPEN, RuleResult.PASS, Deadline.Known(LocalDate(2027, 6, 1)))
        val tba = receipt(EligibilityState.NEEDS_INFO, RuleResult.PASS, Deadline.Tba)

        assertEquals("Assessed rules passed", known.displayCopy().statusLabel)
        assertEquals("Timing needs confirmation", tba.displayCopy().statusLabel)
        assertEquals("Confirm the current application dates.", tba.displayCopy().shortReason)
        assertEquals("Timing: provider date not yet published", tba.displayCopy().timingLabel)
    }

    @Test
    fun `archived cycle takes precedence over an old passed check`() {
        val archived = receipt(EligibilityState.NOT_ACTIONABLE, RuleResult.PASS, Deadline.Tba).copy(
            cycleStatus = OpportunityCycleStatus.ARCHIVED,
        )

        assertEquals("Archived cycle", archived.displayCopy().statusLabel)
        assertEquals("Past cycle; check the provider for a new call.", archived.displayCopy().shortReason)
    }

    @Test
    fun `passing any branch does not surface a failed alternative as the next step`() {
        val passed = receipt(EligibilityState.OPEN, RuleResult.PASS, Deadline.Known(LocalDate(2027, 6, 1))).copy(
            rules = listOf(RuleReceipt("unused-branch", RuleResult.FAIL, "Alternative branch", FactKey.IELTS_SCORE)),
        )

        assertEquals("The checks Porta can assess pass; selection is separate.", passed.displayCopy().shortReason)
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
