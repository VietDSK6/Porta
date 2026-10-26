package io.github.vietdsk6.porta.domain.model

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OpportunityLifecycleTest {
    private val today = LocalDate(2026, 10, 20)

    @Test
    fun `archived cycle remains distinct from a passed deadline`() {
        val closed = opportunity(
            deadline = Deadline.Known(LocalDate(2026, 10, 19)),
        )
        val archived = closed.copy(archived = true)

        assertEquals(OpportunityCycleStatus.CLOSED, closed.cycleStatus(today))
        assertEquals(OpportunityCycleStatus.ARCHIVED, archived.cycleStatus(today))
        assertTrue(closed.isFeasibleForPlanning(LocalDate(2026, 10, 19), minimumDurationDays = 0))
        assertFalse(archived.isFeasibleForPlanning(LocalDate(2026, 10, 19), minimumDurationDays = 0))
    }

    @Test
    fun `freshness applies Porta review window and flags cycle mismatch`() {
        val current = opportunity(
            lastCheckedOn = LocalDate(2026, 7, 22),
            cycleStartOn = LocalDate(2026, 7, 1),
        )
        val due = current.copy(lastCheckedOn = LocalDate(2026, 7, 21))
        val predatesCycle = current.copy(
            lastCheckedOn = LocalDate(2026, 8, 1),
            cycleStartOn = LocalDate(2026, 9, 1),
        )

        assertEquals(SourceFreshness.CURRENT, current.sourceFreshness(today).status)
        assertEquals(SourceFreshness.REVIEW_DUE, due.sourceFreshness(today).status)
        assertEquals(SourceFreshness.PREDATES_CYCLE, predatesCycle.sourceFreshness(today).status)
    }

    @Test
    fun `future review date is never described as current`() {
        val opportunity = opportunity(lastCheckedOn = LocalDate(2026, 10, 21))

        assertEquals(SourceFreshness.FUTURE_DATED, opportunity.sourceFreshness(today).status)
    }

    private fun opportunity(
        deadline: Deadline = Deadline.Tba,
        lastCheckedOn: LocalDate = today,
        cycleStartOn: LocalDate? = null,
    ): Opportunity = Opportunity(
        id = "test-door",
        title = "Test door",
        provider = "Test provider",
        category = OpportunityCategory.SCHOLARSHIP,
        cycle = "test-only",
        cycleStartOn = cycleStartOn,
        sourceUrl = "https://example.invalid/test-door",
        lastCheckedOn = lastCheckedOn,
        deadline = deadline,
        eligibilityRule = RuleNode.ManualCheck("manual", "Manual test check."),
    )
}
